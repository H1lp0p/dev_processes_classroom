package com.stuf.data.infrastructure

import com.stuf.data.model.FeedResponseDtoApiResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FeedResponseJsonParseTest {

    @Test
    fun `parses real backend feed json`() {
        val json =
            """
            {"type":"success","message":null,"data":{"records":[
              {"id":"d3913b33-2366-4adc-8298-6ea2dd4e673e","type":"teaM_TASK","title":"123","createdDate":"2026-05-21T14:02:36.058745"},
              {"id":"b54b26aa-6af2-4508-bddd-5018e52508bf","type":"task","title":"13","createdDate":"2026-05-21T13:09:59.414429"}
            ],"totalRecords":15}}
            """.trimIndent()

        val parsed =
            Serializer.moshi
                .adapter(FeedResponseDtoApiResponse::class.java)
                .fromJson(json)

        assertNotNull(parsed)
        assertEquals(2, parsed?.data?.records?.size)
    }
}
