package java_classes;



//import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WaterDerectorBasic {

    String source_path;
    String out_path;
    int nr_of_files;

    static class Thread_process_color implements Runnable{

        int file_nr;
        String source_path;
        String out_path;

        public Thread_process_color(int file_nr, String source_path, String out_path){
            this.file_nr = file_nr;
            this.source_path = source_path;
            this.out_path = out_path;
        }


        @Override
        public void run() {
            String nif_name = this.source_path + "\\" + Integer.toString(file_nr) + "_nif.tif";
            String green_name = this.source_path + "\\" + Integer.toString(file_nr) + "_green.tif";


            File nif_file = new File(nif_name);
            File green_file = new File(green_name);
            FileInputStream file_nif = null;
            try {
                 file_nif = new FileInputStream(nif_file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedImage nif_img = null;
            BufferedImage green_img = null;


            // open files
            try {
                nif_img = ImageIO.read(file_nif);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                green_img = ImageIO.read(green_file);
            } catch (IOException e) {
                e.printStackTrace();
            }


            /**
             *  Detect water based on NDWI formula IE green-gray/green+gray , water >=0.55
             *  Water are will have a white color and the rest will be black
             */
            if (nif_img != null && green_img != null){
                for (int i = 0; i < nif_img.getWidth(); i++) {
                    for (int j = 0; j < nif_img.getHeight(); j++) {
                        int gray = new Color(nif_img.getRGB(i, j)).getRed();
                        int green = new Color(green_img.getRGB(i, j)).getGreen();
                        float raport = 0;

                        if(green == 0 && gray == 0)
                            raport = (float)0.55;
                        else
                            raport = (float)(green - gray)/(float)(green + gray);

                        if(raport >= 0.425)
                            nif_img.setRGB(i, j, new Color(255, 255, 255).getRGB());
                        else
                            nif_img.setRGB(i, j, new Color(0, 0, 0).getRGB());

                    }

                }

                try {
                    ImageIO.write(nif_img, "PNG", new File(out_path + "\\" + Integer.toString(file_nr) + ".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public WaterDerectorBasic(String source_path, int nr_of_files, String out_path){
        /**
         *  Start a thread pool for each set of files that will need to be processed
         *  Currently it was set for 2 threads because the JVM has only 4 GB allocated,
         *  and for a higher nr of threads more memory will be required
         *
         * @param source_path - path the the folder where user's images are stored
         * @param nr_of_files - nr of sets
         * @param out_path - path to the folder where images containing the water are stored
         */
        this.source_path = source_path;
        this.out_path = out_path;
        this.nr_of_files = nr_of_files;

        ExecutorService excutor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < nr_of_files; i++) {
            excutor.execute(new Thread_process_color(i, this.source_path, this.out_path));
        }



        excutor.shutdown();
        while(!excutor.isTerminated()){

        }


        System.out.println("Finish WATERDETECT");
    }



}
