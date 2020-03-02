package com.ororo.auto.jigokumimi.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.DetectedSongListItemBinding
import com.ororo.auto.jigokumimi.databinding.FragmentDetectedSongListBinding
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.DetectedSongListViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse

/**
 * A simple [Fragment] subclass.
 */
class DetectedSongListFragment : Fragment() {

    private val viewModel: DetectedSongListViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }

        // ViewModelFactory作成
        val viewModelFactory = DetectedSongListViewModel.Factory(activity.application)

        ViewModelProvider(
            viewModelStore,
            viewModelFactory
        ).get(DetectedSongListViewModel::class.java)
    }

    /**
     * RecyclerView Adapter for converting a list of Video to cards.
     */
    private lateinit var viewModelAdapter: DetectedSongListAdapter

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.songlist.observe(viewLifecycleOwner, Observer<List<Song>> { songs ->
            songs?.apply {
                viewModelAdapter?.songs = songs
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val request = viewModel.getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)

        val binding: FragmentDetectedSongListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detected_song_list,
            container,
            false
        )

//         Set the lifecycleOwner so DataBinding can observe LiveData
        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.viewModel = viewModel

        viewModelAdapter = DetectedSongListAdapter()

        binding.recyclerView.adapter = viewModelAdapter

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        AuthorizationClient.openLoginActivity(
            activity,
            Constants.AUTH_TOKEN_REQUEST_CODE,
            request
        )

        binding.refreshButton.setOnClickListener {
            refreshSongs()
        }

        binding.songCountButton.setOnClickListener {
            showSongCount()
        }

        return binding.root
    }

    fun refreshSongs() {
        viewModel.refreshSongsFromRepository()
    }

    fun showSongCount() {
        Toast.makeText(context, viewModelAdapter.itemCount.toString(), Toast.LENGTH_LONG).show()
    }
}

/**
 * RecyclerView Adapter for setting up data binding on the items in the list.
 */
class DetectedSongListAdapter() : RecyclerView.Adapter<DetectedSongListViewHolder>() {

    /**
     * The videos that our Adapter will show
     */
    var songs: List<Song> = emptyList()
        set(value) {
            field = value
            // For an extra challenge, update this to use the paging library.

            // Notify any registered observers that the data set has changed. This will cause every
            // element in our RecyclerView to be invalidated.
            notifyDataSetChanged()
        }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectedSongListViewHolder {
        val withDataBinding: DetectedSongListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            DetectedSongListViewHolder.LAYOUT,
            parent,
            false
        )
        return DetectedSongListViewHolder(withDataBinding)
    }

    override fun getItemCount() = songs.size

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     */
    override fun onBindViewHolder(holder: DetectedSongListViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.song = songs[position]
//            it.videoCallback = callback
        }
    }

}

/**
 * ViewHolder for DevByte items. All work is done by data binding.
 */
class DetectedSongListViewHolder(val viewDataBinding: DetectedSongListItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.detected_song_list_item
    }
}