public class FileDownloader1 {

    public void download(String url) {
        System.out.println("Downloading from: " + url);
        // Simulate delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println("Download interrupted");
        }
    }

    public void cancelDownload() {
        System.out.println("Download canceled.");
    }
}
