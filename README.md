# ShareTango
P2P music sharing Android app

Discover and share songs by connecting to adjacent phones over an ad hoc wireless network.

# Protocol
The app implements a distributed P2P protocol similar to [Napster](https://en.wikipedia.org/wiki/Napster). A node in the network is either a client or a host.
* Join: on start up, the node scans for a neighbouring host. If no host is available, the node becomes a host.
* Publish: once connected, client report its address and list of songs to host
* Query: clients contact host for available songs
* Fetch: retrieve file directly from peer

# Download

[App](https://github.com/junzew/ShareTango/raw/features-junze/sharetango-app-release.apk/)

Requires Android 5.0+

# Screenshots

<table>
<tr>
<td><img width="200px" src="https://github.com/junzew/ShareTango/raw/features-junze/screenshots/device-2017-08-20-175511.png" /></td>
<td><img width="200px" src="https://github.com/junzew/ShareTango/raw/features-junze/screenshots/device-2017-08-20-175703.png" /></td>
<td><img width="200px" src="https://github.com/junzew/ShareTango/raw/features-junze/screenshots/device-2017-08-20-175836.png" /></td>
</tr>
</table>

# Why the name "ShareTango"?
It's a tribute to ["Tango"](https://dota2.gamepedia.com/Tango_(Shared)), an item from the online game Dota 2 that can be shared among players.


