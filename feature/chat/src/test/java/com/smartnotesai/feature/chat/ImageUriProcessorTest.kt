package com.smartnotesai.feature.chat

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.smartnotesai.feature.chat.image.ImageUriProcessor
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import android.content.pm.ProviderInfo

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ImageUriProcessorTest {

    private lateinit var context: Context
    private lateinit var processor: ImageUriProcessor
    private lateinit var provider: TestImageContentProvider
    private lateinit var imageFile: File

    private val imageUri = Uri.parse("content://$AUTHORITY/image")

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        processor = ImageUriProcessor(context)

        imageFile = File.createTempFile("test-image", ".jpg")

        provider = TestImageContentProvider(imageFile, JPEG_MIME_TYPE)

        val providerInfo = ProviderInfo().apply {
            authority = AUTHORITY
            exported = true
            grantUriPermissions = true
        }

        provider.attachInfo(context, providerInfo)

        ShadowContentResolver.registerProviderInternal(
            AUTHORITY,
            provider
        )
    }

    @After
    fun tearDown() {
        imageFile.delete()
    }

    @Test
    fun `process returns image bytes and MIME type`() {
        val expectedBytes = byteArrayOf(1, 2, 3, 4, 5)
        imageFile.writeBytes(expectedBytes)

        val result = processor.process(imageUri)

        assertArrayEquals(expectedBytes, result.bytes)
        assertEquals(JPEG_MIME_TYPE, result.mimeType)
    }

    @Test
    fun `process throws when image is empty`() {
        imageFile.writeBytes(byteArrayOf())

        assertThrows(IllegalArgumentException::class.java) {
            processor.process(imageUri)
        }
    }

    @Test
    fun `process throws for unsupported MIME type`() {
        imageFile.writeBytes(byteArrayOf(1, 2, 3))
        provider.mimeType = "image/gif"

        assertThrows(IllegalArgumentException::class.java) {
            processor.process(imageUri)
        }
    }

    @Test
    fun `process throws when image exceeds maximum size`() {
        imageFile.writeBytes(
            ByteArray(10 * 1024 * 1024 + 1)
        )

        assertThrows(IllegalArgumentException::class.java) {
            processor.process(imageUri)
        }
    }

    private class TestImageContentProvider(
        private val file: File,
        var mimeType: String
    ) : ContentProvider() {

        override fun onCreate(): Boolean = true

        override fun getType(uri: Uri): String = mimeType

        override fun openFile(
            uri: Uri,
            mode: String
        ): ParcelFileDescriptor {
            return ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        }

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?
        ): Cursor? = null

        override fun insert(
            uri: Uri,
            values: ContentValues?
        ): Uri? = null

        override fun delete(
            uri: Uri,
            selection: String?,
            selectionArgs: Array<out String>?
        ): Int = 0

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?
        ): Int = 0
    }

    companion object {
        private const val AUTHORITY = "com.smartnotesai.test.images"
        private const val JPEG_MIME_TYPE = "image/jpeg"
    }
}