package com.ipleiria.estg.meicm.arcismarthome.utils.email

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.Security
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class GMailSender : Authenticator() {
    private val session: Session

    private val arciEmail = "arcismarthome@gmail.com"
    private val arciEmailPassword = "meicm2021"

    companion object {
        init {
            Security.addProvider(JSSEProvider())
        }
    }

    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(arciEmail, arciEmailPassword)
    }

    @Synchronized
    @Throws(Exception::class)
    fun sendMail(subject: String?, body: String, sender: String?, recipients: String) {
        try {
            val message = MimeMessage(session)
            val handler = DataHandler(
                ByteArrayDataSource(body.toByteArray(), "text/plain")
            )

            message.sender =
                if (sender == null) InternetAddress(arciEmail)
                else InternetAddress(sender)

            message.subject = subject
            message.dataHandler = handler
            if (recipients.indexOf(',') > 0) message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipients)
            ) else message.setRecipient(
                Message.RecipientType.TO, InternetAddress(recipients)
            )
            Transport.send(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ByteArrayDataSource(private var data: ByteArray, private var type: String?) :
        DataSource {

        override fun getContentType(): String {
            return type ?: "application/octet-stream"
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            return ByteArrayInputStream(data)
        }

        override fun getName(): String {
            return "ByteArrayDataSource"
        }

        @Throws(IOException::class)
        override fun getOutputStream(): OutputStream {
            throw IOException("Not Supported")
        }
    }

    init {
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.smtp.host", "smtp.gmail.com")
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, this)
    }
}