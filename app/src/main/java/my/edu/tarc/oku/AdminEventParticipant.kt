package my.edu.tarc.oku

import android.app.AlertDialog
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.IO
import my.edu.tarc.oku.data.*
import my.edu.tarc.oku.databinding.FragmentAdminEventParticipantBinding
import android.util.Log
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat
import java.util.*
import kotlin.collections.ArrayList
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.IndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.activation.FileDataSource
import javax.mail.*
import android.os.*
import java.nio.file.DirectoryNotEmptyException
import javax.mail.Message


class AdminEventParticipant : Fragment(), EventRegistrationAdapter.OnItemClickListener {

    private lateinit var binding: FragmentAdminEventParticipantBinding

    //Database
    private val myReg = Firebase.database.getReference("register")
    private val myRef = Firebase.database.getReference("users")
    private val myEve = Firebase.database.getReference("state")

    private var registerList: MutableList<EventRegistration> = ArrayList()
    private var userList: MutableList<User> = ArrayList()
    private var args: AdminEventParticipantArgs? = null
    private val EXTERNAL_STORAGE_PERMISSION_CODE = 23

    private var title: String = ""
    private var date: String = ""
    private var time: String = ""
    private var address: String = ""
    private var link: String = ""
    private var phone: String = ""

    private val job = Job()
    private val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
    private val scopeIO = CoroutineScope(job + IO)

    private lateinit var session: UserSessionManager
    private lateinit var user: HashMap<String?, String?>
    private lateinit var username: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        session = UserSessionManager(requireContext().applicationContext)
        user = session.userDetails
        username = user[UserSessionManager.KEY_NAME].toString()

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_admin_event_participant,
            container,
            false
        )

        args = AdminEventParticipantArgs.fromBundle(requireArguments())

        scopeIO.launch {
            myReg.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    registerList.clear()
                    for (e in snapshot.children) {
                        if (e.key.toString() == args!!.eventId) {
                            binding.tvNoParticipant.visibility = View.INVISIBLE
                            for (m in e.children) {
                                val username = m.key.toString()
                                val date = m.child("date").value.toString()
                                val time = m.child("time").value.toString()
                                val registeredM = EventRegistration(username, date, time)
                                myRef.get().addOnSuccessListener {
                                    Log.i("database", it.value.toString())
                                    if (it.child("member").hasChild(m.key.toString())) {
                                        val userName = it.child("member").child(m.key.toString())
                                            .child("username").value.toString()
                                        val fullName = it.child("member").child(m.key.toString())
                                            .child("fullName").value.toString()
                                        val email = it.child("member").child(m.key.toString())
                                            .child("email").value.toString()
                                        val phoneNo = it.child("member").child(m.key.toString())
                                            .child("phoneNo").value.toString()
                                        val address = it.child("member").child(m.key.toString())
                                            .child("address").value.toString()
                                        val password = it.child("member").child(m.key.toString())
                                            .child("password").value.toString()
                                        val user =
                                            User(
                                                userName,
                                                fullName,
                                                email,
                                                phoneNo,
                                                address,
                                                password
                                            )
                                        userList.add(user)
                                    }
                                }
                                registerList.add(registeredM)
                                Log.i("register", registerList.toString())
                            }
                        }
                    }

                    if (registerList.isEmpty()) {
                        binding.tvNoParticipant.visibility = View.VISIBLE
                    } else {
                        setHasOptionsMenu(true)
                    }
                    scopeMainThread.launch {
                        val myRecyclerView: RecyclerView = binding.eventRecycleView
                        myRecyclerView.adapter =
                            EventRegistrationAdapter(registerList, this@AdminEventParticipant)
                        myRecyclerView.setHasFixedSize(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })

            myEve.get().addOnSuccessListener {
                for (s in it.children) {
                    for (e in s.child("Events").children) {
                        if (e.key.toString() == args!!.eventId) {
                            title = e.child("title").value.toString()
                            date = e.child("date").value.toString()
                            time = e.child("time").value.toString()
                            address = e.child("address").value.toString()
                            link = e.child("link").value.toString()
                            phone = e.child("phone").value.toString()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onPause() {
        job.cancel()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event, menu)
        menu.findItem(R.id.btnAdd).isEnabled = false
        menu.findItem(R.id.btnDelete).isVisible = false
        menu.findItem(R.id.btnParticipants).isVisible = false
        menu.findItem(R.id.btnExport).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.findItem(R.id.btnExport).isVisible = true

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnExport -> {
                var email: String
                myRef.get().addOnSuccessListener {
                    email = it.child("admin").child(username).child("email").value.toString()
                    GlobalScope.launch(IO) {
                        exportExcel(createExcel(createWorkbook()), email)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onItemClick(position: Int) {
        val content = LayoutInflater.from(context)
            .inflate(R.layout.registered_member_info, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val username = content.findViewById<TextView>(R.id.RMUsername)
        val name = content.findViewById<TextView>(R.id.RMName)
        val email = content.findViewById<TextView>(R.id.RMEmail)
        val phoneNo = content.findViewById<TextView>(R.id.RMPhoneNo)
        val address = content.findViewById<TextView>(R.id.RMAddress)

        username.text = ": ${userList[position].username}"
        name.text = ": ${userList[position].fullName}"
        email.text = ": ${userList[position].email}"
        phoneNo.text = ": ${userList[position].phoneNo}"
        address.text = ": ${userList[position].address}"

        builder.setTitle("Member Information")
        builder.setView(content)
        builder.setPositiveButton("Ok") { _, _ -> }
        builder.show()
    }

    private fun createSheetHeader(cellStyle: CellStyle, sheet: Sheet) {
        //setHeaderStyle is a custom function written below to add header style

        //Create sheet first row
        val row = sheet.createRow(7)

        //Header list
        val HEADER_LIST = listOf(
            "Full Name",
            "Username",
            "Phone No.",
            "Email Address",
            "Address",
            "Register Date",
            "Register Time"
        )

        //Loop to populate each column of header row
        for ((index, value) in HEADER_LIST.withIndex()) {

            val columnWidth = (15 * 300)

            //index represents the column number
            sheet.setColumnWidth(index, columnWidth)

            //Create cell
            val cell = row.createCell(index)

            //value represents the header value from HEADER_LIST
            cell?.setCellValue(value)

            //Apply style to cell
            cell.cellStyle = cellStyle
        }
    }

    private fun getHeaderStyle(workbook: Workbook): CellStyle {

        //Cell style for header row
        val cellStyle: CellStyle = workbook.createCellStyle()

        //Apply cell color
        val colorMap: IndexedColorMap = (workbook as XSSFWorkbook).stylesSource.indexedColors
        var color = XSSFColor(IndexedColors.RED, colorMap).indexed
        cellStyle.fillForegroundColor = color
        cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

        //Apply font style on cell text
        val whiteFont = workbook.createFont()
        color = XSSFColor(IndexedColors.WHITE, colorMap).indexed
        whiteFont.color = color
        whiteFont.bold = true
        cellStyle.setFont(whiteFont)


        return cellStyle
    }

    private fun addData(rowIndex: Int, sheet: Sheet) {
        //Add data to each cell
        var index = rowIndex
        for (m in 0 until userList.size) {
            //Create row based on row index
            val row = sheet.createRow(index)
            createCell(row, 0, userList[m].fullName) //Column 1
            createCell(row, 1, userList[m].username) //Column 2
            createCell(row, 2, userList[m].phoneNo) //Column 3
            createCell(row, 3, userList[m].email) //Column 4
            createCell(row, 4, userList[m].address) //Column 5
            createCell(row, 5, registerList[m].date) //Column 6
            createCell(row, 6, registerList[m].time) //Column 7
            index += 1
        }
        sheet.setColumnWidth(3, (15 * 600))
        sheet.setColumnWidth(4, (15 * 800))
        //Loop to populate each column of header row
    }

    private fun createCell(row: Row, columnIndex: Int, value: String?) {
        val cell = row.createCell(columnIndex)
        cell?.setCellValue(value)
    }

    private fun createWorkbook(): Workbook {
        // Creating excel workbook
        val workbook = XSSFWorkbook()

        //Creating first sheet inside workbook
        val sheet: Sheet = workbook.createSheet("Event")

        val titleRow = sheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Title: $title")
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))

        val dateRow = sheet.createRow(1)
        val dateCell = dateRow.createCell(0)
        dateCell.setCellValue("Date: $date")

        val timeRow = sheet.createRow(2)
        val timeCell = timeRow.createCell(0)
        timeCell.setCellValue("Time: $time")

        val phoneRow = sheet.createRow(3)
        val phoneCell = phoneRow.createCell(0)
        phoneCell.setCellValue("Phone No: $phone")

        val addressRow = sheet.createRow(4)
        val addressCell = addressRow.createCell(0)
        addressCell.setCellValue("Address: $address")
        sheet.addMergedRegion(CellRangeAddress(4, 4, 0, 6))

        val linkRow = sheet.createRow(5)
        val linkCell = linkRow.createCell(0)
        linkCell.setCellValue("Website: $link")
        sheet.addMergedRegion(CellRangeAddress(5, 5, 0, 6))

        //Create Header Style For Table
        val cellStyle = getHeaderStyle(workbook)

        //Creating sheet header row
        createSheetHeader(cellStyle, sheet)

        //Adding data to the sheet
        addData(8, sheet)

        return workbook
    }

    @SuppressLint("NewApi")
    private fun createExcel(workbook: Workbook): File {
        // Requesting Permission to access External Storage
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(READ_EXTERNAL_STORAGE),
            EXTERNAL_STORAGE_PERMISSION_CODE
        )

        //Get App Director, APP_DIRECTORY_NAME is a string
        val appDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        //Check App Directory whether it exists or not, create if not.
        if (appDirectory != null && !appDirectory.exists()) {
            appDirectory.mkdir()
        }
        val df: DateFormat = SimpleDateFormat("yyyyMMddhhmmss")
        val dateTime = df.format(Date()).toString()

        //Create excel file with extension .xlsx
        val excelFile = File(appDirectory, "Report$dateTime.xlsx")

        //Write workbook to file using FileOutputStream
        try {
            FileOutputStream(excelFile).use { outputStream ->
                workbook.write(outputStream)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return excelFile
    }

    @SuppressLint("NewApi")
    private fun exportExcel(filename: File, email: String) {

        val to = "$email"  //receiver email
        val from = "okuapplication@gmail.com" //Sender email
        val properties = System.getProperties()

        with(properties) {
            put("mail.smtp.host", "smtp.gmail.com") //Configure smtp host
            put("mail.smtp.port", "587") //Configure port
            put("mail.smtp.starttls.enable", "true") //Enable TLS
            put("mail.smtp.auth", "true") //Enable authentication
        }

        val auth = object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(from, "S3cre7P@55") //Credentials of the sender email
        }

        val session = Session.getDefaultInstance(properties, auth)

        try {
            val message = MimeMessage(session)

            with(message) {
                setFrom(InternetAddress(from))

                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                subject = "Report for event \"$title\"" //Email subject

                val source: DataSource = FileDataSource(filename)
                dataHandler = DataHandler(source)
                val filepath = filename.toString().split("/")
                fileName = filepath.last()
            }
            Transport.send(message)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Excel file generated and sent to your email.",
                    Toast.LENGTH_LONG
                ).show()
            }

            //Delete file from storage when email sent
            try {
                filename.delete()
            } catch (x: NoSuchFileException) {
                x.printStackTrace()
            } catch (x: DirectoryNotEmptyException) {
                x.printStackTrace()
            } catch (x: IOException) {
                x.printStackTrace()
            }
        } catch (e: MessagingException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
}