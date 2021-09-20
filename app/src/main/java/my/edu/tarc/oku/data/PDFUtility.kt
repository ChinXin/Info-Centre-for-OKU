package my.edu.tarc.oku.data
//
//import com.itextpdf.text.pdf.Barcode128
//
//import com.itextpdf.text.pdf.PdfWriter
//
//import android.graphics.Bitmap
//
//import android.graphics.BitmapFactory
//
//import android.graphics.PorterDuff
//
//import android.graphics.PorterDuffColorFilter
//
//import com.itextpdf.text.pdf.PdfPCell
//
//import com.itextpdf.text.pdf.PdfPTable
//
//import android.graphics.drawable.BitmapDrawable
//
//import android.R
//import android.content.Context
//
//import androidx.core.content.ContextCompat
//
//import android.graphics.drawable.Drawable
//import android.util.Log
//import androidx.annotation.NonNull
//import com.itextpdf.text.*
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.io.FileOutputStream
//
//import java.lang.Exception
//import java.lang.NullPointerException
//
//
//internal object PDFUtility {
//    private val TAG = PDFUtility::class.java.simpleName
//    private val FONT_TITLE: Font = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD)
//    private val FONT_SUBTITLE: Font = Font(Font.FontFamily.TIMES_ROMAN, 10f, Font.NORMAL)
//    private val FONT_CELL: Font = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
//    private val FONT_COLUMN: Font = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)
//    @Throws(Exception::class)
//    fun createPdf(
//        @NonNull mContext: Context,
//        mCallback: OnDocumentClose?,
//        items: List<Array<String>>,
//        @NonNull filePath: String,
//        isPortrait: Boolean
//    ) {
//        if (filePath == "") {
//            throw NullPointerException("PDF File Name can't be null or blank. PDF File can't be created")
//        }
//        val file = File(filePath)
//        if (file.exists()) {
//            file.delete()
//            Thread.sleep(50)
//        }
//        val document = Document()
//        document.setMargins(24f, 24f, 32f, 32f)
//        document.setPageSize(if (isPortrait) PageSize.A4 else PageSize.A4.rotate())
//        val pdfWriter = PdfWriter.getInstance(document, FileOutputStream(filePath))
//        pdfWriter.setFullCompression()
//        pdfWriter.pageEvent = PageNumeration()
//        document.open()
//        setMetaData(document)
//        addHeader(mContext, document)
//        addEmptyLine(document, 3)
//        document.add(createDataTable(items))
//        addEmptyLine(document, 2)
//        document.add(createSignBox())
//        document.close()
//        try {
//            pdfWriter.close()
//        } catch (ex: Exception) {
//            Log.e(TAG, "Error While Closing pdfWriter : $ex")
//        }
//        mCallback?.onPDFDocumentClose(file)
//    }
//
//    @Throws(DocumentException::class)
//    private fun addEmptyLine(document: Document, number: Int) {
//        for (i in 0 until number) {
//            document.add(Paragraph(" "))
//        }
//    }
//
//    private fun setMetaData(document: Document) {
//        document.addCreationDate()
//        //document.add(new Meta("",""));
//        document.addAuthor("RAVEESH G S")
//        document.addCreator("RAVEESH G S")
//        document.addHeader("DEVELOPER", "RAVEESH G S")
//    }
//
//    @Throws(Exception::class)
//    private fun addHeader(mContext: Context, document: Document) {
//        val table = PdfPTable(3)
//        table.widthPercentage = 100f
//        table.setWidths(floatArrayOf(2f, 7f, 2f))
//        table.defaultCell.border = PdfPCell.NO_BORDER
//        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
//        table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
//        var cell: PdfPCell
//        run {
//
//            /*LEFT TOP LOGO*/
//            val d = ContextCompat.getDrawable(mContext, R.mipmap.logo)
//            val bmp = (d as BitmapDrawable?)!!.bitmap
//            val stream = ByteArrayOutputStream()
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val logo: Image = Image.getInstance(stream.toByteArray())
//            logo.setWidthPercentage(80)
//            logo.scaleToFit(105, 55)
//            cell = PdfPCell(logo)
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.isUseAscender = true
//            cell.border = PdfPCell.NO_BORDER
//            cell.setPadding(2f)
//            table.addCell(cell)
//        }
//        run {
//
//            /*MIDDLE TEXT*/cell = PdfPCell()
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.border = PdfPCell.NO_BORDER
//            cell.setPadding(8f)
//            cell.isUseAscender = true
//            var temp = Paragraph("I AM TITLE", FONT_TITLE)
//            temp.alignment = Element.ALIGN_CENTER
//            cell.addElement(temp)
//            temp = Paragraph("I am Subtitle", FONT_SUBTITLE)
//            temp.alignment = Element.ALIGN_CENTER
//            cell.addElement(temp)
//            table.addCell(cell)
//        }
//        /* RIGHT TOP LOGO*/run {
//            val logoTable = PdfPTable(1)
//            logoTable.widthPercentage = 100f
//            logoTable.defaultCell.border = PdfPCell.NO_BORDER
//            logoTable.horizontalAlignment = Element.ALIGN_CENTER
//            logoTable.defaultCell.verticalAlignment = Element.ALIGN_CENTER
//            val drawable = ContextCompat.getDrawable(mContext, R.mipmap.logo)
//            val bmp = (drawable as BitmapDrawable?)!!.bitmap
//            val stream = ByteArrayOutputStream()
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val logo: Image = Image.getInstance(stream.toByteArray())
//            logo.setWidthPercentage(80)
//            logo.scaleToFit(38, 38)
//            var logoCell = PdfPCell(logo)
//            logoCell.horizontalAlignment = Element.ALIGN_CENTER
//            logoCell.verticalAlignment = Element.ALIGN_MIDDLE
//            logoCell.border = PdfPCell.NO_BORDER
//            logoTable.addCell(logoCell)
//            logoCell = PdfPCell(Phrase("Logo Text", FONT_CELL))
//            logoCell.horizontalAlignment = Element.ALIGN_CENTER
//            logoCell.verticalAlignment = Element.ALIGN_MIDDLE
//            logoCell.border = PdfPCell.NO_BORDER
//            logoCell.setPadding(4f)
//            logoTable.addCell(logoCell)
//            cell = PdfPCell(logoTable)
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.isUseAscender = true
//            cell.border = PdfPCell.NO_BORDER
//            cell.setPadding(2f)
//            table.addCell(cell)
//        }
//
//        //Paragraph paragraph=new Paragraph("",new Font(Font.FontFamily.TIMES_ROMAN, 2.0f, Font.NORMAL, BaseColor.BLACK));
//        //paragraph.add(table);
//        //document.add(paragraph);
//        document.add(table)
//    }
//
//    @Throws(DocumentException::class)
//    private fun createDataTable(dataTable: List<Array<String>>): PdfPTable {
//        val table1 = PdfPTable(2)
//        table1.widthPercentage = 100f
//        table1.setWidths(floatArrayOf(1f, 2f))
//        table1.headerRows = 1
//        table1.defaultCell.verticalAlignment = Element.ALIGN_CENTER
//        table1.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
//        var cell: PdfPCell
//        run {
//            cell = PdfPCell(Phrase("COLUMN - 1", FONT_COLUMN))
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.setPadding(4f)
//            table1.addCell(cell)
//            cell = PdfPCell(Phrase("COLUMN - 2", FONT_COLUMN))
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.setPadding(4f)
//            table1.addCell(cell)
//        }
//        val top_bottom_Padding = 8f
//        val left_right_Padding = 4f
//        var alternate = false
//        val lt_gray = BaseColor(221, 221, 221) //#DDDDDD
//        var cell_color: BaseColor?
//        val size = dataTable.size
//        for (i in 0 until size) {
//            cell_color = if (alternate) lt_gray else BaseColor.WHITE
//            val temp = dataTable[i]
//            cell = PdfPCell(Phrase(temp[0], FONT_CELL))
//            cell.horizontalAlignment = Element.ALIGN_LEFT
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.paddingLeft = left_right_Padding
//            cell.paddingRight = left_right_Padding
//            cell.paddingTop = top_bottom_Padding
//            cell.paddingBottom = top_bottom_Padding
//            cell.backgroundColor = cell_color
//            table1.addCell(cell)
//            cell = PdfPCell(Phrase(temp[1], FONT_CELL))
//            cell.horizontalAlignment = Element.ALIGN_CENTER
//            cell.verticalAlignment = Element.ALIGN_MIDDLE
//            cell.paddingLeft = left_right_Padding
//            cell.paddingRight = left_right_Padding
//            cell.paddingTop = top_bottom_Padding
//            cell.paddingBottom = top_bottom_Padding
//            cell.backgroundColor = cell_color
//            table1.addCell(cell)
//            alternate = !alternate
//        }
//        return table1
//    }
//
//    @Throws(DocumentException::class)
//    private fun createSignBox(): PdfPTable {
//        val outerTable = PdfPTable(1)
//        outerTable.widthPercentage = 100f
//        outerTable.defaultCell.border = PdfPCell.NO_BORDER
//        val innerTable = PdfPTable(2)
//        run {
//            innerTable.widthPercentage = 100f
//            innerTable.setWidths(floatArrayOf(1f, 1f))
//            innerTable.defaultCell.border = PdfPCell.NO_BORDER
//
//            //ROW-1 : EMPTY SPACE
//            var cell = PdfPCell()
//            cell.border = PdfPCell.NO_BORDER
//            cell.fixedHeight = 60f
//            innerTable.addCell(cell)
//
//            //ROW-2 : EMPTY SPACE
//            cell = PdfPCell()
//            cell.border = PdfPCell.NO_BORDER
//            cell.fixedHeight = 60f
//            innerTable.addCell(cell)
//
//            //ROW-3 : Content Left Aligned
//            cell = PdfPCell()
//            var temp =
//                Paragraph(Phrase("Signature of Supervisor", FONT_SUBTITLE))
//            cell.addElement(temp)
//            temp = Paragraph(Phrase("( RAVEESH G S )", FONT_SUBTITLE))
//            temp.paddingTop = 4f
//            temp.alignment = Element.ALIGN_LEFT
//            cell.addElement(temp)
//            cell.horizontalAlignment = Element.ALIGN_LEFT
//            cell.border = PdfPCell.NO_BORDER
//            cell.setPadding(4f)
//            innerTable.addCell(cell)
//
//            //ROW-4 : Content Right Aligned
//            cell = PdfPCell(Phrase("Signature of Staff ", FONT_SUBTITLE))
//            cell.horizontalAlignment = Element.ALIGN_RIGHT
//            cell.border = PdfPCell.NO_BORDER
//            cell.setPadding(4f)
//            innerTable.addCell(cell)
//        }
//        val signRow = PdfPCell(innerTable)
//        signRow.horizontalAlignment = Element.ALIGN_LEFT
//        signRow.border = PdfPCell.NO_BORDER
//        signRow.setPadding(4f)
//        outerTable.addCell(signRow)
//        return outerTable
//    }
//
//    @Throws(Exception::class)
//    private fun getImage(imageByte: ByteArray, isTintingRequired: Boolean): Image {
//        val paint = Paint()
//        if (isTintingRequired) {
//            paint.setColorFilter(PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN))
//        }
//        val input = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)
//        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(output)
//        canvas.drawBitmap(input, 0, 0, paint)
//        val stream = ByteArrayOutputStream()
//        output.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        val image: Image = Image.getInstance(stream.toByteArray())
//        image.setWidthPercentage(80)
//        return image
//    }
//
//    private fun getBarcodeImage(pdfWriter: PdfWriter, barcodeText: String): Image {
//        val barcode = Barcode128()
//        //barcode.setBaseline(-1); //BARCODE TEXT ABOVE
//        barcode.font = null
//        barcode.code = barcodeText
//        barcode.codeType = Barcode128.CODE128
//        barcode.textAlignment = Element.ALIGN_BASELINE
//        return barcode.createImageWithBarcode(pdfWriter.directContent, BaseColor.BLACK, null)
//    }
//
//    interface OnDocumentClose {
//        fun onPDFDocumentClose(file: File?)
//    }
//}