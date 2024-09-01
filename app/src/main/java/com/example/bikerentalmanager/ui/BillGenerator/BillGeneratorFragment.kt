package com.example.bikerentalmanager.ui.BillGenerator

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.bikerentalmanager.BuildConfig
import com.example.bikerentalmanager.R
import com.example.bikerentalmanager.databinding.FragmentBillgeneratorBinding
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.html.WebColors
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar

class BillGeneratorFragment : Fragment() {

    private var _binding: FragmentBillgeneratorBinding? = null
    private val binding get() = _binding!!

    // Initialize colors for table headers
    var headColor: BaseColor = WebColors.getRGBColor("#DEDEDE")
    var tableHeadColor: BaseColor = WebColors.getRGBColor("#F5ABAB")

    // EditText fields for user inputs
    private var editTextHelmet: EditText? = null
    private var editTextDateOfBooking: EditText? = null
    private var editTextBookingID: EditText? = null
    private var editTextKMLimit: EditText? = null
    private var editStartingKM: EditText? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillgeneratorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize your EditTexts from the binding
        editTextHelmet = binding.editTextHelmet
        editTextDateOfBooking = binding.editTextDateOfBooking
        editTextBookingID = binding.editTextBookingID
        editTextKMLimit = binding.editTextKMLimit
        editStartingKM = binding.editStartingKM

        // Set up click listener for generating the bill
        binding.buttonGenerateBill.setOnClickListener { view ->
            createPassPDF()
            requestPermission()
            Snackbar.make(view, "Generating Bill...", Snackbar.LENGTH_LONG)
                .setAnchorView(binding.buttonGenerateBill)
                .setAction("Action", null).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Request WRITE_EXTERNAL_STORAGE permission if not already granted
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            // Show an explanation to the user why the permission is needed
        } else {
            // Request the permission
            requestPermissions(arrayOf(permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    // Share the generated PDF file using WhatsApp
    private fun sharePdfFile(file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"

        Log.e("PDFFile", "Share file path: " + file.absolutePath)
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file
        )

        // Set up the intent for sharing
        intent.clipData = ClipData.newRawUri("", fileUri)
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, "917039241820")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.setPackage("com.whatsapp")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    // Create a PDF document based on user inputs
    private fun createPassPDF() {
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a")
        @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("ddMMyyyy_HHmm")

        // Retrieve user inputs safely from EditText fields
        val helmet = view?.findViewById<EditText>(R.id.editTextHelmet)?.text.toString().trim()
        val dateOfBooking = view?.findViewById<EditText>(R.id.editTextDateOfBooking)?.text.toString().trim()
        val bookingID = view?.findViewById<EditText>(R.id.editTextBookingID)?.text.toString().trim()
        val kmLimit = view?.findViewById<EditText>(R.id.editTextKMLimit)?.text.toString().trim()
        val startingKM = view?.findViewById<EditText>(R.id.editStartingKM)?.text.toString().trim()
        val endingKM = view?.findViewById<EditText>(R.id.editEndingKM)?.text.toString().trim()
        val totalKM = view?.findViewById<EditText>(R.id.editTotalKM)?.text.toString().trim()
        val idDetails = view?.findViewById<EditText>(R.id.editIDDetails)?.text.toString().trim()
        val hirerName = view?.findViewById<EditText>(R.id.editHirerName)?.text.toString().trim()
        val hirerMobNo = view?.findViewById<EditText>(R.id.editHirerMobNo)?.text.toString().trim()
        val relativeName = view?.findViewById<EditText>(R.id.editRelativeName)?.text.toString().trim()
        val relativeMobNo = view?.findViewById<EditText>(R.id.editRelativeMobNo)?.text.toString().trim()
        val vehicleName = view?.findViewById<EditText>(R.id.editVehicleName)?.text.toString().trim()
        val localAdd = view?.findViewById<EditText>(R.id.editLocalAdd)?.text.toString().trim()
        val startingDate = view?.findViewById<EditText>(R.id.editStartingDate)?.text.toString().trim()
        val startingTime = view?.findViewById<EditText>(R.id.editStartingTime)?.text.toString().trim()
        val returnDate = view?.findViewById<EditText>(R.id.editReturnDate)?.text.toString().trim()
        val returnTime = view?.findViewById<EditText>(R.id.editReturnTime)?.text.toString().trim()
        val securityDeposit = view?.findViewById<EditText>(R.id.editSecurityDeposit)?.text.toString().trim()
        val actualRent = view?.findViewById<EditText>(R.id.editActualRent)?.text.toString().trim()
        val lateTime = view?.findViewById<EditText>(R.id.editLateTime)?.text.toString().trim()
        val finalTotal = view?.findViewById<EditText>(R.id.editFinalTotal)?.text.toString().trim()

        try {
            // Define the PDF file name and directory
            val fileName = "BikeRentalInvoice_" + dateFormat.format(Calendar.getInstance().time) + ".pdf"
            val dirName = "docs"

            // Create directory if it doesn't exist
            val dir = File(requireContext().filesDir, dirName)
            if (!dir.exists()) {
                val isDirectoryCreated = dir.mkdir()
                Log.e("Directory", "Created: $isDirectoryCreated")
            }

            // Create the PDF file
            val pdfFile = File(dir, fileName)
            if (!pdfFile.exists()) {
                if (pdfFile.createNewFile()) {
                    Log.e("PDFFile", "File Created: ${pdfFile.absolutePath}")
                }
            }

            // Set up the FileOutputStream and Document
            val fos = FileOutputStream(pdfFile)
            val document = Document()
            PdfWriter.getInstance(document, fos)

            document.open()

            // Header Section
            val headerTable = PdfPTable(2)
            headerTable.widthPercentage = 100f

            // Add the title and address (left cell)
            var headerCell = PdfPCell()
            headerCell.border = PdfPCell.NO_BORDER
            headerCell.addElement(
                Paragraph(
                    "RENTAL POINT",
                    Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
                )
            )
            headerCell.addElement(
                Paragraph(
                    "All Types Bike & Car Available On Rent",
                    Font(Font.FontFamily.HELVETICA, 10f)
                )
            )
            headerCell.addElement(
                Paragraph(
                    "Address: 3rd Floor - 35, Ellora Plaza, Near Railway Station, Maharani Road, Indore (M.P.) 452007",
                    Font(Font.FontFamily.HELVETICA, 8f)
                )
            )
            headerTable.addCell(headerCell)

            // Add Booking ID and Date (right cell)
            headerCell = PdfPCell()
            headerCell.border = PdfPCell.NO_BORDER
            headerCell.addElement(
                Paragraph(
                    "Date: " + format.format(Calendar.getInstance().time), Font(
                        Font.FontFamily.HELVETICA, 10f
                    )
                )
            )
            headerCell.addElement(
                Paragraph(
                    "Booking ID: $bookingID",
                    Font(Font.FontFamily.HELVETICA, 10f)
                )
            )
            headerTable.addCell(headerCell)

            document.add(headerTable)

            // Add some space
            document.add(Paragraph(" "))

            // Information Section
            val infoTable = PdfPTable(4)
            infoTable.widthPercentage = 100f

            // Add K.M. Limit, Starting K.M., Ending K.M., Total K.M.
            infoTable.addCell(createCell("K.M. Limit: $kmLimit"))
            infoTable.addCell(createCell("Starting K.M.: $startingKM"))
            infoTable.addCell(createCell("Ending K.M.: $endingKM"))
            infoTable.addCell(createCell("Total K.M.: $totalKM"))

            // Add Hirer details
            infoTable.addCell(createCell("Hirer Name: $hirerName"))
            infoTable.addCell(createCell("Mob. No.: $hirerMobNo"))
            infoTable.addCell(createCell("Relative Name: $relativeName"))
            infoTable.addCell(createCell("Mob. No.: $relativeMobNo"))

            // Add Vehicle and Rental details
            infoTable.addCell(createCell("Vehicle Name: $vehicleName"))
            infoTable.addCell(createCell("Local Add.: $localAdd"))
            infoTable.addCell(createCell("Starting Date: $startingDate"))
            infoTable.addCell(createCell("Starting Time: $startingTime"))

            infoTable.addCell(createCell("Return Date: $returnDate"))
            infoTable.addCell(createCell("Return Time: $returnTime"))
            infoTable.addCell(createCell("Security Deposit: $securityDeposit"))
            infoTable.addCell(createCell("Late Time: $lateTime"))

            document.add(infoTable)

            // Add some space
            document.add(Paragraph(" "))

            // Rent and Total Section
            val rentTable = PdfPTable(2)
            rentTable.widthPercentage = 100f
            rentTable.addCell(createCell("Rent: ________"))
            rentTable.addCell(createCell("Total: ________", PdfPCell.ALIGN_RIGHT))

            document.add(rentTable)

            // Add space for signature
            document.add(Paragraph(" "))

            // Customer Signature Line
            document.add(Paragraph("Customer Signature: ______________________"))

            document.close()
            fos.close()

            Toast.makeText(requireContext(), "PDF created in >> ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
            sharePdfFile(pdfFile)
        } catch (e: Throwable) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper method to create a cell with alignment
    private fun createCell(text: String, alignment: Int = PdfPCell.ALIGN_LEFT): PdfPCell {
        val cell = PdfPCell(Paragraph(text, Font(Font.FontFamily.HELVETICA, 10f)))
        cell.setPadding(5f)
        cell.horizontalAlignment = alignment
        cell.border = PdfPCell.BOX
        return cell
    }
}
//package com.example.bikerentalmanager.ui.BillGenerator
//
//import android.Manifest.permission
//import android.annotation.SuppressLint
//import android.content.ActivityNotFoundException
//import android.content.ClipData
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.fragment.app.Fragment
//import com.example.bikerentalmanager.BuildConfig
//import com.example.bikerentalmanager.R
//import com.example.bikerentalmanager.databinding.FragmentBillgeneratorBinding
//import com.google.android.material.snackbar.Snackbar
//import com.itextpdf.text.BaseColor
//import com.itextpdf.text.Document
//import com.itextpdf.text.Font
//import com.itextpdf.text.Paragraph
//import com.itextpdf.text.html.WebColors
//import com.itextpdf.text.pdf.PdfPCell
//import com.itextpdf.text.pdf.PdfPTable
//import com.itextpdf.text.pdf.PdfWriter
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.Calendar
//
//
//class BillGeneratorFragment : Fragment() {
//
//    private var _binding: FragmentBillgeneratorBinding? = null
//    private val binding get() = _binding!!
//
//    private val cell: PdfPCell? = null
//
//    var headColor: BaseColor = WebColors.getRGBColor("#DEDEDE")
//    var tableHeadColor: BaseColor = WebColors.getRGBColor("#F5ABAB")
//
//    private var editTextHelmet: EditText? = null
//    private var editTextDateOfBooking: EditText? = null
//    private var editTextBookingID: EditText? = null
//    private var editTextKMLimit: EditText? = null
//    private var editStartingKM: EditText? = null
//
//    companion object {
//        private const val PERMISSION_REQUEST_CODE = 200
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentBillgeneratorBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
//        // Initialize your EditTexts
//        editTextHelmet = binding.editTextHelmet
//        editTextDateOfBooking = binding.editTextDateOfBooking
//        editTextBookingID = binding.editTextBookingID
//        editTextKMLimit = binding.editTextKMLimit
//        editStartingKM = binding.editStartingKM
//
//        binding.buttonGenerateBill.setOnClickListener { view ->
//            createPassPDF()
//            requestPermission()
//            Snackbar.make(view, "Generating Bill...", Snackbar.LENGTH_LONG)
//                .setAnchorView(binding.buttonGenerateBill)
//                .setAction("Action", null).show()
//        }
//
//        return root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//
//    private fun requestPermission() {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                permission.WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permission already granted
//        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
//                requireActivity(),
//                permission.WRITE_EXTERNAL_STORAGE
//            )
//        ) {
//            // Show an explanation to the user why the permission is needed
//        } else {
//            requestPermissions(arrayOf(permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
//        }
//    }
//
//    private fun sharePdfFile(file: File) {
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "application/pdf"
//
//        Log.e("PDFFile", "Share file path: " + file.absolutePath)
//        val fileUri = FileProvider.getUriForFile(
//            requireContext(),
//            BuildConfig.APPLICATION_ID + ".fileprovider",
//            file
//        )
//
//        intent.clipData = ClipData.newRawUri("", fileUri)
//        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, "917039241820")
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
//        intent.setPackage("com.whatsapp")
//
//        try {
//            startActivity(intent)
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show()
//        }
//    }
//    private fun createPassPDF() {
//        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a")
//        @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("ddMMyyyy_HHmm")
//
//        // Retrieve user inputs safely
//        val helmet = view?.findViewById<EditText>(R.id.editTextHelmet)?.text.toString().trim()
//        val dateOfBooking = view?.findViewById<EditText>(R.id.editTextDateOfBooking)?.text.toString().trim()
//        val bookingID = view?.findViewById<EditText>(R.id.editTextBookingID)?.text.toString().trim()
//        val kmLimit = view?.findViewById<EditText>(R.id.editTextKMLimit)?.text.toString().trim()
//        val startingKM = view?.findViewById<EditText>(R.id.editStartingKM)?.text.toString().trim()
//        val endingKM = view?.findViewById<EditText>(R.id.editEndingKM)?.text.toString().trim()
//        val totalKM = view?.findViewById<EditText>(R.id.editTotalKM)?.text.toString().trim()
//        val idDetails = view?.findViewById<EditText>(R.id.editIDDetails)?.text.toString().trim()
//        val hirerName = view?.findViewById<EditText>(R.id.editHirerName)?.text.toString().trim()
//        val hirerMobNo = view?.findViewById<EditText>(R.id.editHirerMobNo)?.text.toString().trim()
//        val relativeName = view?.findViewById<EditText>(R.id.editRelativeName)?.text.toString().trim()
//        val relativeMobNo = view?.findViewById<EditText>(R.id.editRelativeMobNo)?.text.toString().trim()
//        val vehicleName = view?.findViewById<EditText>(R.id.editVehicleName)?.text.toString().trim()
//        val localAdd = view?.findViewById<EditText>(R.id.editLocalAdd)?.text.toString().trim()
//        val startingDate = view?.findViewById<EditText>(R.id.editStartingDate)?.text.toString().trim()
//        val startingTime = view?.findViewById<EditText>(R.id.editStartingTime)?.text.toString().trim()
//        val returnDate = view?.findViewById<EditText>(R.id.editReturnDate)?.text.toString().trim()
//        val returnTime = view?.findViewById<EditText>(R.id.editReturnTime)?.text.toString().trim()
//        val securityDeposit = view?.findViewById<EditText>(R.id.editSecurityDeposit)?.text.toString().trim()
//        val actualRent = view?.findViewById<EditText>(R.id.editActualRent)?.text.toString().trim()
//        val lateTime = view?.findViewById<EditText>(R.id.editLateTime)?.text.toString().trim()
//        val finalTotal = view?.findViewById<EditText>(R.id.editFinalTotal)?.text.toString().trim()
//
//        try {
//            val fileName = "BikeRentalInvoice_" + dateFormat.format(Calendar.getInstance().time) + ".pdf"
//            val dirName = "docs"
//
//            val dir = File(requireContext().filesDir, dirName)
//            if (!dir.exists()) {
//                val isDirectoryCreated = dir.mkdir()
//                Log.e("Directory", "Created: $isDirectoryCreated")
//            }
//
//            val pdfFile = File(dir, fileName)
//            if (!pdfFile.exists()) {
//                if (pdfFile.createNewFile()) {
//                    Log.e("PDFFile", "File Created: ${pdfFile.absolutePath}")
//                }
//            }
//
//            val fos = FileOutputStream(pdfFile)
//            val document = Document()
//            PdfWriter.getInstance(document, fos)
//
//            document.open()
//
//            // Header Section
//            val headerTable = PdfPTable(2)
//            headerTable.widthPercentage = 100f
//
//            // Add the title and address (right cell)
//            var headerCell = PdfPCell()
//            headerCell.border = PdfPCell.NO_BORDER
//            headerCell.addElement(
//                Paragraph(
//                    "RENTAL POINT",
//                    Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
//                )
//            )
//            headerCell.addElement(
//                Paragraph(
//                    "All Types Bike & Car Available On Rent",
//                    Font(Font.FontFamily.HELVETICA, 10f)
//                )
//            )
//            headerCell.addElement(
//                Paragraph(
//                    "Address: 3rd Floor - 35, Ellora Plaza, Near Railway Station, Maharani Road, Indore (M.P.) 452007",
//                    Font(Font.FontFamily.HELVETICA, 8f)
//                )
//            )
//            headerTable.addCell(headerCell)
//
//            // Add Booking ID and Date
//            headerCell = PdfPCell()
//            headerCell.border = PdfPCell.NO_BORDER
//            headerCell.addElement(
//                Paragraph(
//                    "Date: " + format.format(Calendar.getInstance().time), Font(
//                        Font.FontFamily.HELVETICA, 10f
//                    )
//                )
//            )
//            headerCell.addElement(
//                Paragraph(
//                    "Booking ID: $bookingID",
//                    Font(Font.FontFamily.HELVETICA, 10f)
//                )
//            )
//            headerTable.addCell(headerCell)
//
//            document.add(headerTable)
//
//            // Space
//            document.add(Paragraph(" "))
//
//            // Information Section
//            val infoTable = PdfPTable(4)
//            infoTable.widthPercentage = 100f
//
//            // Add the K.M. Limit, Starting K.M., Ending K.M., Total K.M.
//            infoTable.addCell(createCell("K.M. Limit: $kmLimit"))
//            infoTable.addCell(createCell("Starting K.M.: $startingKM"))
//            infoTable.addCell(createCell("Ending K.M.: $endingKM"))
//            infoTable.addCell(createCell("Total K.M.: $totalKM"))
//
//            // Add Hirer details
//            infoTable.addCell(createCell("Hirer Name: $hirerName"))
//            infoTable.addCell(createCell("Mob. No.: $hirerMobNo"))
//            infoTable.addCell(createCell("Relative Name: $relativeName"))
//            infoTable.addCell(createCell("Mob. No.: $relativeMobNo"))
//
//            infoTable.addCell(createCell("Vehicle Name: $vehicleName"))
//            infoTable.addCell(createCell("Local Add.: $localAdd"))
//            infoTable.addCell(createCell("Starting Date: $startingDate"))
//            infoTable.addCell(createCell("Starting Time: $startingTime"))
//
//            infoTable.addCell(createCell("Return Date: $returnDate"))
//            infoTable.addCell(createCell("Return Time: $returnTime"))
//            infoTable.addCell(createCell("Security Deposit: $securityDeposit"))
//            infoTable.addCell(createCell("Late Time: $lateTime"))
//
//            document.add(infoTable)
//
//            // Space
//            document.add(Paragraph(" "))
//
//            // Rent and Total
//            val rentTable = PdfPTable(2)
//            rentTable.widthPercentage = 100f
//            rentTable.addCell(createCell("Rent: ________"))
//            rentTable.addCell(createCell("Total: ________", PdfPCell.ALIGN_RIGHT))
//
//
//            document.add(rentTable)
//
//            // Space
//            document.add(Paragraph(" "))
//
//            // Customer Signature
//            document.add(Paragraph("Customer Signature: ______________________"))
//
//            document.close()
//            fos.close()
//
//            Toast.makeText(requireContext(), "PDF created in >> ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
//            sharePdfFile(pdfFile)
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Helper method to create a cell with alignment
//    private fun createCell(text: String, alignment: Int = PdfPCell.ALIGN_LEFT): PdfPCell {
//        val cell = PdfPCell(Paragraph(text, Font(Font.FontFamily.HELVETICA, 10f)))
//        cell.setPadding(5f)
//        cell.horizontalAlignment = alignment
//        cell.border = PdfPCell.BOX
//        return cell
//    }
//}
