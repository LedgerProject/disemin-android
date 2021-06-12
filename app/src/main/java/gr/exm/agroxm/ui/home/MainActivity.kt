package gr.exm.agroxm.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import gr.exm.agroxm.R
import gr.exm.agroxm.databinding.ActivityMainBinding
import gr.exm.agroxm.ui.FieldDetailActivity
import gr.exm.agroxm.ui.Navigator
import gr.exm.agroxm.ui.main.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MainActivity : AppCompatActivity(), KoinComponent {

    private val model: MainViewModel by viewModels()
    private val navigator: Navigator by inject()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.logout) {
                model.logout()
                navigator.showSplash(this)
                finish()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings)
        )

        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)*/

        // Listen for fragment interactions
        model.selectedField.observe(this, {
            val intent = Intent(this, FieldDetailActivity::class.java)
                .putExtra(FieldDetailActivity.ARG_FIELD, it)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        })
    }
}