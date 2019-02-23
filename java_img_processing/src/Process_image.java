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

         String source_images = "D:\\proj_licenta\\set_images_landsat";
         String out_processed_images = "D:\\proj_licenta\\set_images_landsat\\processed_landsat";


         String path_source_images = "D:\\proj_licenta\\set_images";
         String path_processed_images = "D:\\proj_licenta\\set_images\\processed_images";
         String out_splited_source_images = "D:\\proj_licenta\\set_images\\iamges_splited";
         String out_splited_processed_images = "D:\\proj_licenta\\set_images\\processed_images_splited";

         ///just exclude the 3'rd image bad format
//         WaterDerectorBasic w = new WaterDerectorBasic(source_images,8, out_processed_images);
//         Subimages_512 sub = new Subimages_512(21, path_source_images, out_splited_source_images, path_processed_images, out_splited_processed_images);
//        make_difference diff= new make_difference("D:\\proj_licenta\\set_images_landsat\\processed_landsat\\out3.png", "D:\\proj_licenta\\set_images_landsat\\processed_landsat\\out4.png");




        String topo_path = "D:\\proj_licenta\\set_images_landsat\\merged_final_cairo_il.tif";
        String nif_path = "D:\\proj_licenta\\set_images_landsat\\6_nif.tif";
        String result_nif_path = "D:\\proj_licenta\\set_images_landsat\\processed_landsat\\6.png";
//        File topo_file = new File(topo_string);
//        BufferedImage img_topo = null;
//
//        try {
//            img_topo = ImageIO.read(topo_file);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//
//        for (int i = 0; i < img_topo.getWidth(); i++) {
//            for (int j = 0; j < img_topo.getHeight(); j++) {
//                Color x = new Color(img_topo.getRGB(i ,j));
//                System.out.println(new Integer(x.getRed()).toString() + " " + new Integer(x.getBlue()).toString() + " " + new Integer(x.getGreen()).toString());
//            }
//        }



//        pixel to coordinates gdal ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        gdal.AllRegister();
//
//        File f = new File(topo_string);
//
//        System.out.println(f.exists());
//
//        Dataset ds = gdal.Open(topo_string, gdalconst.GA_ReadOnly);
//
//
//        double[] transformation = ds.GetGeoTransform();
//
//
//        System.out.println(transformation.length);
//
//        for (int i = 0; i < transformation.length; i++) {
//            System.out.println(transformation[i]);
//        }
//
//        double x = 0;
//        double y = 0;
//
//        double rez_x = transformation[1]*x + transformation[2]*y + transformation[0];
//        double rez_y = transformation[4]*x + transformation[5]*y + transformation[3];
//
//        System.out.println(rez_x);
//        System.out.println(rez_y);
//
//        SpatialReference crs = new SpatialReference();
//        crs.ImportFromWkt(ds.GetProjectionRef());
//
//        SpatialReference crsGeo = new SpatialReference();
//
//        crsGeo.ImportFromEPSG(4326);
//        CoordinateTransformation t = new CoordinateTransformation(crs, crsGeo);
//        double[] rez = t.TransformPoint(rez_x, rez_y);
//
//        System.out.println(new Double(rez[0]).toString() + " " +  new Double(rez[1]).toString());

        CreateFloodMapBasedOnTOPO flood = new CreateFloodMapBasedOnTOPO(result_nif_path, nif_path, topo_path);



    }
}
