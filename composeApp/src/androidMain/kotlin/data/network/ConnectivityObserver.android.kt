package data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

actual class ConnectivityObserver(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    actual fun observe(): Flow<NetworkStatus> {
        return  callbackFlow {
            val callback =
                object  : ConnectivityManager.NetworkCallback(){
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        launch { send(NetworkStatus.Available) }
                    }

                    override fun onLosing(network: Network, maxMsToLive: Int) {
                        super.onLosing(network, maxMsToLive)
                        launch { send(NetworkStatus.Losing) }
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        launch { send(NetworkStatus.Lost) }
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        launch { send(NetworkStatus.Unavailable) }
                    }
                }

            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }

}