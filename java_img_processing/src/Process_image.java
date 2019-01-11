import com.sun.jmx.remote.internal.ClientNotifForwarder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class Process_image {


     static class Thread_image implements Runnable{

         private String image_name;
         private int thread_nr;

         public Thread_image(String img_name, int thread_nr){
             this.image_name = img_name;
             this.thread_nr = thread_nr;
         }

         @Override
         public void run() {
             java.io.File f = new File(this.image_name);
             BufferedImage img = null;
             try {
                 img = ImageIO.read(f);
             } catch (IOException e) {
                 e.printStackTrace();
             }



             for (int i = 0; i < img.getWidth(); i++) {
                 for (int j = 0; j < img.getHeight(); j++) {
                     if ((new Color(img.getRGB(i, j))).getRed() <= 5)
                         img.setRGB(i, j, new Color(255, 255, 255).getRGB());
                     else
                         img.setRGB(i, j, new Color(0, 0, 0).getRGB());
                 }
             }

             try {
                 ImageIO.write(img, "jpg", new File("modified" + Integer.toString(this.thread_nr) + ".jpg"));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }


    public static void main(String []args) throws IOException {

         String source_images = "D:\\proj_licenta\\set_images";
         String out_processed_images = "D:\\proj_licenta\\set_images\\processed_images";


         String path_source_images = "D:\\proj_licenta\\set_images";
         String path_processed_images = "D:\\proj_licenta\\set_images\\processed_images";
         String out_splited_source_images = "D:\\proj_licenta\\set_images\\iamges_splited";
         String out_splited_processed_images = "D:\\proj_licenta\\set_images\\processed_images_splited";

         ///just exclude the 3'rd image bad format
//         WaterDerectorBasic w = new WaterDerectorBasic(source_images,21, out_processed_images);
         Subimages_512 sub = new Subimages_512(21, path_source_images, out_splited_source_images, path_processed_images, out_splited_processed_images);


//        File f  = new File(out_splited_source_images + "\\0_0.png");
//        BufferedImage bf = ImageIO.read(f);
//
//        BufferedImage newbuf = new BufferedImage(bf.getWidth(), bf.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
//
//        for (int i = 0; i < bf.getWidth(); i++) {
//            for (int j = 0; j < bf.getHeight(); j++) {
//                newbuf.setRGB(i, j, bf.getRGB(i, j));
//            }
//        }
//
//        ImageIO.write(newbuf, "png", new File(out_splited_source_images + "\\0_00.png"));

    }
}
