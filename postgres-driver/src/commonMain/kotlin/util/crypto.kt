package one.keia.oss.psql.driver.util

import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.macs.hmac.sha2.HmacSHA256
import kotlin.experimental.xor

internal fun hmac(key: ByteArray, message: ByteArray): ByteArray =
    HmacSHA256(key).doFinal(message)

internal fun sha256(message: ByteArray): ByteArray =
    SHA256().digest(message)

// ported from the go crypto library:
// https://cs.opensource.google/go/x/crypto/+/refs/tags/v0.16.0:pbkdf2/pbkdf2.go;l=42
internal fun pbkdf2HmacSHA256DeriveKey(
    key: ByteArray,
    salt: ByteArray,
    iterations: Int,
    keyLength: Int,
): ByteArray {
    val prf = HmacSHA256(key)
    val hashLen = prf.macLength()
    val numBlocks = (keyLength + hashLen - 1) / hashLen

    val buf = ByteArray(hashLen)
    lateinit var dk: ByteArray
    var u = ByteArray(hashLen)
    for (block in 1..numBlocks) {
        prf.reset()
        prf.update(salt)

        buf[0] = (block shr 24).toByte()
        buf[1] = (block shr 16).toByte()
        buf[2] = (block shr 8).toByte()
        buf[3] = block.toByte()

        prf.update(buf, 0, 4)
        dk = prf.doFinal()
        dk.copyInto(u, startIndex = dk.size - hashLen)

        for (n in 2..iterations) {
            prf.reset()
            prf.update(u)

            u = prf.doFinal()
            for (i in u.indices) {
                dk[i] = dk[i] xor u[i]
            }
        }
    }

    return dk
}
