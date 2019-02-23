package java_classes;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.*;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

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
         String source_images = args[0];
         String out_processed_images = args[1];
         int nr_images = new File(source_images).listFiles().length / 3;


        ///just exclude the 3'rd image bad format
        WaterDerectorBasic w = new WaterDerectorBasic(source_images, nr_images, out_processed_images);
        CreateFloodMapBasedOnTOPO flood_predictor = new CreateFloodMapBasedOnTOPO(source_images, out_processed_images, nr_images);
//         Subimages_512 sub = new Subimages_512(21, path_source_images, out_splited_source_images, path_processed_images, out_splited_processed_images);


//        CreateFloodMapBasedOnTOPO flood = new CreateFloodMapBasedOnTOPO(result_nif_path, nif_path, topo_path);
//        make_difference diff= new make_difference("D:\\proj_licenta\\set_images_landsat\\processed_landsat\\7.png", "D:\\proj_licenta\\set_images_landsat\\processed_landsat\\out7.png");




    }
}
