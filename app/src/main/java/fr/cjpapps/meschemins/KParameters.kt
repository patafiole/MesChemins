package fr.cjpapps.meschemins

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import fr.cjpapps.meschemins.databinding.ActivityKparametersBinding

class KParameters : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityKparametersBinding
    val mesprefs : SharedPreferences = MyHelper.getInstance().recupPrefs()
    val editeur : SharedPreferences.Editor = mesprefs.edit()
    var gpsInterval : Int = mesprefs.getInt("gps_interval", 5)
    var filterLength : Int = mesprefs.getInt("filter_length", 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKparametersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        when(gpsInterval) {
            2 -> binding.included.deux.isChecked = true
            5 -> binding.included.cinq.isChecked = true
            10 -> binding.included.dix.isChecked = true
        }
        when(filterLength) {
            1 -> binding.included.z1.isChecked = true
            3 -> binding.included.z3.isChecked = true
            5 -> binding.included.z5.isChecked = true
            7 -> binding.included.z7.isChecked = true
        }

        binding.included.groupfreq.setOnCheckedChangeListener { _, checkedId ->
            val rB : RadioButton = findViewById(checkedId)
            val valeur : String = rB.text as String
            editeur.putInt("gps_interval", valeur.toInt())
            editeur.apply()
            Log.i("APPCHEMINS", "GPS interval set = "+valeur)
        }

        binding.included.groupfiltre.setOnCheckedChangeListener { _, checkedId ->
            val rB : RadioButton = findViewById(checkedId)
            val valeur : String = rB.text as String
            editeur.putInt("filter_length", valeur.toInt())
            editeur.apply()
            Log.i("APPCHEMINS", "longueur filtre set = "+valeur)
        }

        binding.included.buttonfin.setOnClickListener {
            finish()
        }
    }

}