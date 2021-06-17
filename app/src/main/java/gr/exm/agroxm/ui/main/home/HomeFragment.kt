package gr.exm.agroxm.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import gr.exm.agroxm.data.Field
import gr.exm.agroxm.data.Status
import gr.exm.agroxm.databinding.FragmentHomeBinding
import gr.exm.agroxm.ui.addfield.AddFieldActivity
import gr.exm.agroxm.ui.main.MainViewModel
import gr.exm.agroxm.util.REQUEST_CODE_ADD_FIELD
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private val mainViewModel: MainViewModel by activityViewModels()

    init {
        lifecycleScope.launch {
            whenCreated {
                homeViewModel = ViewModelProvider(this@HomeFragment).get(HomeViewModel::class.java)
                homeViewModel.fetch()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val adapter = FieldAdapter {
            mainViewModel.selectField(it)
        }
        binding.recycler.adapter = adapter

        binding.addField.setOnClickListener {
            startActivityForResult(Intent(context, AddFieldActivity::class.java), REQUEST_CODE_ADD_FIELD)
            //Toast.makeText(requireActivity(), "Coming soon!", Toast.LENGTH_LONG).show()
        }

        binding.swiperefresh.setOnRefreshListener {
            homeViewModel.fetch()
        }

        homeViewModel.data.observe(viewLifecycleOwner, {
            Timber.d("Data updated: ${it.status}")
            when (it.status) {
                Status.SUCCESS -> {
                    binding.swiperefresh.isRefreshing = false
                    if (!it.data.isNullOrEmpty()) {
                        adapter.submitList(it.data)
                        binding.recycler.visibility = View.VISIBLE
                        binding.empty.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                    } else {
                        binding.empty.visibility = View.VISIBLE
                        binding.empty.title("You haven't added any fields yet")
                        binding.empty.subtitle("Add your field by clicking the + button")
                        binding.empty.listener(null)
                        binding.recycler.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                    }
                }
                Status.ERROR -> {
                    binding.swiperefresh.isRefreshing = false
                    binding.empty.title("Oops! Could not find any fields.")
                    binding.empty.subtitle(it.message)
                    binding.empty.action("Retry")
                    binding.empty.listener { homeViewModel.fetch() }
                    binding.empty.visibility = View.VISIBLE
                    binding.recycler.visibility = View.GONE
                    binding.progress.visibility = View.GONE
                }
                Status.LOADING -> {
                    binding.progress.visibility =
                        if (binding.swiperefresh.isRefreshing) View.GONE else View.VISIBLE
                    binding.empty.visibility = View.GONE
                }
            }
        })

        return binding.root
    }
}

fun interface OnFieldSelectedListener {
    fun onFieldSelected(field: Field)
}
