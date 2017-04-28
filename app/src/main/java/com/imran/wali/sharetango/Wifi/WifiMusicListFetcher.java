package com.imran.wali.sharetango.Wifi;

/**
 * Created by wali on 21/02/17.
 */

public class WifiMusicListFetcher  {

 //   public static ArrayList<MusicData> getMusicList(Context mContext, WifiP2pDevice device){
//        String host;
//        int port;
//        int len;
//        Socket socket = new Socket();
//        byte buf[]  = new byte[1024];
//        try {
//            /**
//             * Create a client socket with the host,
//             * port, and timeout information.
//             */
//            socket.bind(null);
//            socket.connect((new InetSocketAddress(host, port)), 500);
//
//            /**
//             * Create a byte stream from a JPEG file and pipe it to the output stream
//             * of the socket. This data will be retrieved by the server device.
//             */
//            OutputStream outputStream = socket.getOutputStream();
//            ContentResolver cr = context.getContentResolver();
//            InputStream inputStream = null;
//            inputStream = cr.openInputStream(Uri.parse("path/to/picture.jpg"));
//            while ((len = inputStream.read(buf)) != -1) {
//                outputStream.write(buf, 0, len);
//            }
//            outputStream.close();
//            inputStream.close();
//        } catch (FileNotFoundException e) {
//            //catch logic
//        } catch (IOException e) {
//            //catch logic
//        }
//
///**
// * Clean up any open sockets when done
// * transferring or if an exception occurred.
// */
//        finally {
//            if (socket != null) {
//                if (socket.isConnected()) {
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        //catch logic
//                    }
//                }
//            }
//        }
//    }
}
