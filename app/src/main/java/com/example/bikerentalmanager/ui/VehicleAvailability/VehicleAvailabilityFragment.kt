package com.example.bikerentalmanager.ui.VehicleAvailability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bikerentalmanager.databinding.FragmentVehicleavailabilityBinding

class VehicleAvailabilityFragment : Fragment() {

    private var _binding: FragmentVehicleavailabilityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vehicleAvailabilityViewModel =
            ViewModelProvider(this).get(VehicleAvailabilityViewModel::class.java)

        _binding = FragmentVehicleavailabilityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textVehicleAvailability
        vehicleAvailabilityViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}