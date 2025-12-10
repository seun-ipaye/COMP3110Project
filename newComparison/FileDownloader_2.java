public class FileDownloader2 {

    public void fetchFile(String url) {
        System.out.println("Connecting to " + url);
        simulateDownload();
    }

    public void stop() {
        System.out.println("Stopped downloading.");
    }

    private void simulateDownload() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            System.out.println("Download failed.");
        }
    }
}
