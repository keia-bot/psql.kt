package one.keia.oss.psql.driver.connection

import kotlinx.coroutines.coroutineScope
import naibu.ext.intoOrNull
import one.keia.oss.psql.driver.Credentials
import one.keia.oss.psql.driver.auth.SCRAM
import one.keia.oss.psql.driver.util.hexMD5
import one.keia.oss.psql.protocol.Message

public suspend fun auth(connection: Connection): Unit = coroutineScope {
    when (val auth = connection.read0<Message.Backend.Authentication>()) {
        Message.Backend.Authentication.ClearTextPassword -> {
            val password = connection.resources.credentials.intoOrNull<Credentials.Basic>()?.password
                           ?: error("Server requested Clear-Text-Password but client didn't configure one.")

            connection.send0(Message.Frontend.PasswordMessage(password), flush = true)
        }

        is Message.Backend.Authentication.MD5Password    -> {
            val credentials = connection.resources.credentials.intoOrNull<Credentials.Basic>()
            if (credentials?.password == null) {
                error("Server requested MD5-Password but client didn't configure a password")
            }

            val digested =
                "md5" + hexMD5(hexMD5(credentials.password + credentials.username) + auth.salt.decodeToString())
            connection.send0(Message.Frontend.PasswordMessage(digested), flush = true)
        }

        is Message.Backend.Authentication.SASL           -> {
            require(SCRAM.SHA_256 in auth.mechanisms) {
                "server does not support $SCRAM.SHA_256"
            }

            val password = connection.resources.credentials.intoOrNull<Credentials.Basic>()?.password
                           ?: error("Server requested SASL authentication but client didn't configure a password.")

            val client = SCRAM.Client(SCRAM.generateClientNonce(), password)

            //
            connection.send0(
                Message.Frontend.SASLInitialResponse(SCRAM.SHA_256, client.createClientFirstMessage()),
                flush = true
            )

            //
            val serverFirstMessage = connection.read0<Message.Backend.Authentication.SASLContinue>().data
            client.handleServerFirstMessage(serverFirstMessage)
            connection.send0(Message.Frontend.SASLResponse(client.createClientFinalMessage()), flush = true)

            // final
            val serverFinalMessage = connection.read0<Message.Backend.Authentication.SASLFinal>().data
            client.handleServerFinalMessage(serverFinalMessage)
        }

        Message.Backend.Authentication.GSS               -> TODO()

        Message.Backend.Authentication.SSPI              -> TODO()

        Message.Backend.Authentication.KerberosV5        -> TODO()

        else                                             -> error("invalid authentication message: $auth")
    }

    connection.read0<Message.Backend.Authentication>()
}
