package it.fulminazzo.creeper.runner

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Stepwise
class ServerRunnerFunctionalTest extends Specification {
    private static final int PORT = 17526
    private static final long READ_TIMEOUT_SECONDS = 60

    @Shared
    private Thread runnerThread

    @Shared
    private Socket client

    void setupSpec() {
        ProcessHandler.enableDebug()
        def workDir = new File('build/resources/functionalTest')

        def eulaFile = new File(workDir, 'eula.txt')
        eulaFile.delete()
        eulaFile.parentFile.mkdirs()
        eulaFile << 'eula=true'

        runnerThread = new Thread(() -> {
            ServerRunner.main(*[
                    PORT.toString(),
                    workDir.absolutePath,
                    'java',
                    '-Xms256M',
                    '-Xmx1G',
                    '-jar',
                    'paper-1.8.8-445.jar',
                    'nogui'
            ])
        })
        runnerThread.daemon = true
    }

    void cleanupSpec() {
        runnerThread?.interrupt()
        try {
            client?.close()
        } catch (IOException ignored) {
            // do not log any error
        }
    }

    def 'step 1: test that runner correctly outputs Minecraft server output'() {
        given:
        runnerThread.start()

        when:
        sleep(1_000)
        client = new Socket('0.0.0.0', PORT)

        then:
        noExceptionThrown()

        when:
        def line = readClientStream('Done')

        then:
        line =~ /\[[0-9]{2}:[0-9]{2}:[0-9]{2}[^]]*INFO[^]]*]: *Done \([0-9]+\.[0-9]+s\)!/
    }

    def 'step 2: test that output is correctly forwarded to Minecraft server and result is returned'() {
        when:
        def output = client.outputStream
        output.write('Hello, world!\n'.bytes)
        output.flush()

        then:
        noExceptionThrown()

        when:
        def line = readClientStream('Unknown command')

        then:
        line =~ /\[[0-9]{2}:[0-9]{2}:[0-9]{2}[^]]*INFO[^]]*]: *Unknown command. Type "\/help" for help./
    }

    def 'step 3: test that client is able to stop Minecraft server'() {
        when:
        def output = client.outputStream
        output.write('stopprocess\n'.bytes)
        output.flush()

        then:
        noExceptionThrown()

        when:
        def line = readClientStream('Internal process terminated')

        then:
        line == 'Internal process terminated'

        and:
        !serverAlive
    }

    private String readClientStream(final String until) {
        def future = CompletableFuture.supplyAsync {
            def reader = new InputStreamReader(client.inputStream)
            String line
            while ((line = reader.readLine()) != null)
                if (line.contains(until)) return line
            return line
        }
        return future.get(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    private static boolean isServerAlive() {
        try {
            new Socket('0.0.0.0', PORT).close()
            return true
        } catch (IOException ignored) {
            return false
        }
    }

}
