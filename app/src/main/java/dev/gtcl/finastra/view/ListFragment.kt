package dev.gtcl.finastra.view

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dev.gtcl.finastra.R
import dev.gtcl.finastra.databinding.FragmentListBinding
import dev.gtcl.finastra.model.Photo
import dev.gtcl.finastra.view.list.PictureAdapter
import dev.gtcl.finastra.viewmodel.ListViewModel
import dev.gtcl.finastra.viewmodel.ViewModelFactory

class ListFragment: Fragment() {

    private var binding: FragmentListBinding? = null

    private val viewModel: ListViewModel by lazy {
        val viewModelFactory = ViewModelFactory()
        ViewModelProvider(this, viewModelFactory).get(ListViewModel::class.java)
    }

    private val sol by lazy {
        val result = try {
            ListFragmentArgs.fromBundle(requireArguments()).sol
        } catch (e : Exception){
            0
        }
        result
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentListBinding.inflate(inflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ListFragment.viewModel
            recyclerView.adapter = PictureAdapter(object : PictureAdapter.PhotoClickListener{
                override fun onClick(photo: Photo) {
                    findNavController().navigate(ListFragmentDirections.actionShowDetail(photo))
                }
            })
            swipeRefreshLayout.setOnRefreshListener { this@ListFragment.viewModel.fetchPhotos(sol) }
        }

        viewModel.apply {
            if(photos.value == null)
                fetchPhotos(sol)
            errorMessage.observe(viewLifecycleOwner, {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            })
            loading.observe(viewLifecycleOwner, {
                binding?.swipeRefreshLayout?.isRefreshing = it
            })
        }

        val activity = activity as AppCompatActivity
        activity.supportActionBar?.apply {
            title = requireContext().getString(R.string.list_fragment_label, sol)
            val navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val enableBackButton = navHostFragment.childFragmentManager.backStackEntryCount > 0
            setDisplayHomeAsUpEnabled(enableBackButton)
            setHomeButtonEnabled(enableBackButton)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> (activity as MainActivity).onSupportNavigateUp()
            R.id.refresh -> viewModel.fetchPhotos(sol)
            R.id.next -> findNavController().navigate(ListFragmentDirections.actionViewNextSol(sol + 1))
        }
        return true
    }

    // To prevent memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}