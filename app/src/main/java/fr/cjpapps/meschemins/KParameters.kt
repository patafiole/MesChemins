package fr.cjpapps.meschemins

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import fr.cjpapps.meschemins.databinding.ActivityKparametersBinding
import fr.cjpapps.meschemins.databinding.ContentKparametersBinding

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
        binding.included.choixfrequ.text = "zozo"
        setSupportActionBar(binding.toolbar)

        when(gpsInterval) {
            2 -> binding.included.deux.setChecked(true)
            5 -> binding.included.cinq.setChecked(true)
            10 -> binding.included.dix.setChecked(true)
        }
        when(filterLength) {
            0 -> binding.included.z0?.setChecked(true)
            3 -> binding.included.z3?.setChecked(true)
            5 -> binding.included.z5?.setChecked(true)
            7 -> binding.included.z7?.setChecked(true)
        }

        binding.included.groupfreq.setOnCheckedChangeListener { group, checkedId ->
            val rB : RadioButton = findViewById(checkedId)
            val valeur : String = rB.text as String
            editeur.putInt("gps_interval", valeur.toInt())
            editeur.apply()
            Log.i("APPCHEMINS", "GPS interval = "+valeur)
        }

        binding.included.groupfiltre.setOnCheckedChangeListener { group, checkedId ->
            val rB : RadioButton = findViewById(checkedId)
            val valeur : String = rB.text as String
            editeur.putInt("gps_interval", valeur.toInt())
            editeur.apply()
            Log.i("APPCHEMINS", "longueur filtre = "+valeur)
        }

        binding.included.buttonfin.setOnClickListener() { view ->
            finish()
        }
    }

}