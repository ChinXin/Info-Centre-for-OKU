package my.edu.tarc.oku.data

import android.util.Log
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter

import java.lang.Exception

internal class PageNumeration : PdfPageEventHelper() {
    override fun onEndPage(writer: PdfWriter, document: Document) {
        try {
            var cell: PdfPCell
            val table = PdfPTable(2)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 1f))

            //1st Column
            cell = PdfPCell(Phrase("OKU Event Registered Member List", FONT_FOOTER))
            cell.horizontalAlignment = Element.ALIGN_LEFT
            cell.border = 0
            cell.setPadding(2f)
            table.addCell(cell)
            table.setTotalWidth(
                document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin()
            )
            table.writeSelectedRows(
                0,
                -1,
                document.leftMargin(),
                document.bottomMargin() - 5,
                writer.directContent
            )

            //2nd Column
            cell = PdfPCell(Phrase("Page - " + writer.pageNumber.toString(), FONT_FOOTER))
            cell.horizontalAlignment = Element.ALIGN_RIGHT
            cell.border = 0
            cell.setPadding(2f)
            table.addCell(cell)
            table.setTotalWidth(
                document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin()
            )
            table.writeSelectedRows(
                0,
                -1,
                document.leftMargin(),
                document.bottomMargin() - 5,
                writer.directContent
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e(TAG, ex.toString())
        }
    }

    companion object {
        private val TAG = PageNumeration::class.java.simpleName
        private val FONT_FOOTER = Font(Font.FontFamily.TIMES_ROMAN, 8f, Font.NORMAL, BaseColor.DARK_GRAY)
    }
}