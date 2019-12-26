package codes.chrishorner.socketweather

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import java.util.ArrayDeque
import java.util.Deque

class MainActivity : AppCompatActivity(), Screen.Navigator {

  private val backstack: Deque<Screen> = ArrayDeque(3)
  private lateinit var rootContainer: ViewGroup

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    rootContainer = BuildTypeConfig.getRootContainerFor(this)

    val backstackEntries = savedInstanceState?.getIntArray("backstack")
        ?.map { Screen.values()[it] }
        ?: listOf(Screen.ChooseLocation)

    backstack.addAll(backstackEntries)

    val initialView = backstackEntries[0].getView(rootContainer)
    rootContainer.addView(initialView)
    getCurrentScreen().bind(getCurrentScreenView(), this)
  }

  override fun onDestroy() {
    super.onDestroy()
    getCurrentScreen().unbind(getCurrentScreenView())
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putIntArray("backstack", backstack.map { it.ordinal }.toIntArray())
  }

  override fun onBackPressed() {
    if (backstack.size > 1) {
      goBack()
    } else {
      // Guard against a leak introduced in Android 10.
      // https://twitter.com/Piwai/status/1169274622614704129
      finishAfterTransition()
    }
  }

  override fun goTo(screen: Screen) {
    getCurrentScreen().unbind(getCurrentScreenView())
    rootContainer.removeAllViews()
    backstack.push(screen)
    val view = screen.getView(rootContainer)
    rootContainer.addView(view)
    screen.bind(view, this)
  }

  override fun goBack() {
    getCurrentScreen().unbind(getCurrentScreenView())
    backstack.pop()
    rootContainer.removeAllViews()
    val screen = getCurrentScreen()
    val view = screen.getView(rootContainer)
    rootContainer.addView(view)
    screen.bind(view, this)
  }

  private fun getCurrentScreen(): Screen = backstack.peek() ?: throw IllegalStateException("No current screen.")

  private fun getCurrentScreenView(): View = rootContainer[0]
}
