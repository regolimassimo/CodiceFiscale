package it.massimoregoli.codicefiscale

import android.os.Bundle
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.core.widget.doAfterTextChanged
import it.massimoregoli.codicefiscale.databinding.ActivityMainBinding
import androidx.lifecycle.Observer
import it.massimoregoli.codicefiscale.adapters.MyArrayAdapter
import it.massimoregoli.codicefiscale.viewmodels.*
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "CF2021"
        private const val FILENAME = "countrycode.csv"
    }

    private lateinit var binding: ActivityMainBinding
    private var fiscalCode = CFModel()
    private var cFviewModel = CFViewModel()
    private val countryList = mutableListOf<CountryModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewModel()
        setUIController()
    }

    private fun setUIController() {
        binding.etLastName.doAfterTextChanged { name ->
            fiscalCode.lastName = name.toString()
            cFviewModel.getFiscalCode(fiscalCode)
        }
        binding.etFirstName.doAfterTextChanged { name ->
            fiscalCode.firstName = name.toString()
            cFviewModel.getFiscalCode(fiscalCode)
        }
        binding.etBirtdate.doAfterTextChanged { name ->
            fiscalCode.birthDate = name.toString()
            cFviewModel.getFiscalCode(fiscalCode)
        }

        binding.etCountryName.setOnItemClickListener { adapterView, view, i, l ->
            fiscalCode.birthPlace =  (adapterView.adapter.getItem(i) as CountryModel).code
            cFviewModel.getFiscalCode(fiscalCode)
        }
        binding.etCountryName.doAfterTextChanged { name ->
            findSingleCountry(name.toString())
        }


        readFile()
    }

    private fun findSingleCountry(name: String) {
        CoroutineScope(Dispatchers.Default).launch {
            fiscalCode.birthPlace = "X999"
            for (country in countryList) {
                if (country.description.toUpperCase(Locale.getDefault()) == name.toUpperCase(Locale.getDefault())) {
                    fiscalCode.birthPlace = country.code
                    break
                }
            }
            cFviewModel.getFiscalCode(fiscalCode)
        }
    }

    private fun readFile() {
        binding.etCountryName.isEnabled = false
        binding.etCountryName.hint = "Wait Please"
        val reader = assets.open(FILENAME)
            .bufferedReader()
        CoroutineScope(Dispatchers.IO).launch {
            reader.useLines { lines ->
                lines.forEach {
                    val fields = it.toUpperCase(Locale.getDefault()).split(";")
                    countryList.add(CountryModel(fields[0], fields[1], fields[2]))
                }
                launch(Dispatchers.Default) {
                    countryList.sortBy { it.description }
                    launch(Dispatchers.Main) {
                        Log.w(TAG, "LETTO!")
                        binding.etCountryName.isEnabled = true
                        val adapter = MyArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            countryList
                        )
                        binding.etCountryName.setAdapter(adapter)
                        binding.etCountryName.hint = "Birthplace"
                    }
                }
            }
        }
    }

    private fun setViewModel() {
        val fiscalCodeObserver = Observer<String> { cf ->
            Log.w(TAG, cf)
            binding.tvFiscalCode.text = cf
        }
        cFviewModel.fiscalCode.observe(this, fiscalCodeObserver)

//        val countryObserver = Observer<List<CountryModel>> {c ->
//            binding.etCountryName.setAdapter(ArrayAdapter<CountryModel>(this,
//                android.R.layout.simple_dropdown_item_1line, c))
//        }
//        countryViewModel.listOfCountries.observe(this, countryObserver)
    }



}