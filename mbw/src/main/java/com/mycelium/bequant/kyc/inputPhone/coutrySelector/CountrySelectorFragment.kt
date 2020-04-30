package com.mycelium.bequant.kyc.inputPhone.coutrySelector;

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.mycelium.bequant.Constants.ACTION_COUNTRY_SELECTED
import com.mycelium.bequant.Constants.COUNTRY_MODEL_KEY
import com.mycelium.bequant.kyc.BequantKycViewModel
import com.mycelium.wallet.R
import com.mycelium.wallet.databinding.ActivityBequantKycCountryOfResidenceBinding
import kotlinx.android.synthetic.main.activity_bequant_kyc_country_of_residence.*
import java.util.*

class CountrySelectorFragment : Fragment() {

    lateinit var viewModel: CountrySelectorViewModel
    private lateinit var activityViewModel: BequantKycViewModel
    private var showPhoneCode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CountrySelectorViewModel::class.java)
        showPhoneCode = arguments?.getBoolean("showPhoneCode") ?: true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.run {
            activityViewModel = ViewModelProviders.of(this).get(BequantKycViewModel::class.java)
        } ?: throw Throwable("invalid activity")
        activityViewModel.updateActionBarTitle("Country of Residence")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            DataBindingUtil.inflate<ActivityBequantKycCountryOfResidenceBinding>(inflater, R.layout.activity_bequant_kyc_country_of_residence, container, false)
                    .apply {
                        viewModel = this@CountrySelectorFragment.viewModel
                        lifecycleOwner = this@CountrySelectorFragment
                    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val countryModels: List<CountryModel> = countryCodes.map {
            CountryModel(
                    name = countriesMap[it] ?: "Unknown",
                    acronym = it,
                    code = PhoneNumberUtil.getInstance().getCountryCodeForRegion(it))
        }
        rvCountries.addItemDecoration(DividerItemDecoration(rvCountries.context, DividerItemDecoration.VERTICAL))
        val adapter = CountriesAdapter(object : CountriesAdapter.ItemClickListener {
            override fun onItemClick(countryModel: CountryModel) {
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent(ACTION_COUNTRY_SELECTED)
                        .putExtra(COUNTRY_MODEL_KEY, countryModel))
                if (targetFragment == null) {
                    activityViewModel.country.value = countryModel
                    findNavController().popBackStack()
                } else {
                    targetFragment?.onActivityResult(targetRequestCode, RESULT_OK,
                            Intent().putExtra(COUNTRY_MODEL_KEY, countryModel))
                    activity?.onBackPressed()
                }
            }
        }).apply {
            submitList(countryModels)
        }
        adapter.showPhoneCode = showPhoneCode
        rvCountries.adapter = adapter
        viewModel.search.observe(viewLifecycleOwner, Observer { text ->
            if (text.isNullOrEmpty()) {
                adapter.submitList(countryModels)
            } else {
                val filter = countryModels.filter {
                    it.name.toLowerCase().contains(text.toLowerCase()) || it.acronym.toLowerCase().contains(text.toLowerCase())
                }
                adapter.submitList(filter)
            }
        })
    }

    val countriesMap = Locale.getISOCountries()
            .associate { it to Locale("", it).displayCountry }

    val countryCodes = "TJ JM HT ST MS AE PK NL LU BZ IR BO UY GH SA CI MF TF AI QA SX LY BV PG KG GQ EH NU PR GD KR HM SM SL CD MK TR DZ GE PS BB UA GP PF NA BW SY TG DO AQ CH MG FO VG GI BN LA IS EE UM LT RS MR AD HU TK MY AO CV NF PA GW BE PT GB IM US YE HK AZ CC ML SK VU TL HR SR MU CZ PM LS WS KM IT BI WF GN SG CO CN AW MA FI VA ZW KY BH PY EC LR RU PL OM MT SS DE TM SJ MM TT IL BD NR LK UG NG BQ MX CW SI MN CA AX VN TW JP IO RO BG GU BR AM ZM DJ JE AT CM SE FJ KZ GL GY CX MW TN ZA TO CY MV PN RW NI KN BJ ET GM TZ VC FK SD MC AU CL DK FR TC CU AL MZ BS NE GT LI NP BF PW KW IN GA TV MO SH MD CK AR SC IE ES LB BM RE KI AG MQ SV JO TH SO MH CG KP GF BA YT GS KE PE BT SZ CR TD DM NC GR GG HN VI CF SN AF MP PH BY LV NO EG KH IQ LC NZ BL UZ ID ER VE FM SB ME AS".split(" ")

}
