package com.calebwolin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.ObjectWriter; 


import okhttp3.*;


public class App {
    public static String URL = "https://www.facebook.com/marketplace/boise/vehicles?maxPrice=5000&sortBy=creation_time_descend&exact=false";
    public static void main(String[] args) 
    {
        List<Vehicle> lastList = getListings();
        List<Vehicle> carList = lastList;
        send_notification("Starting the search for an epic car...");
        int timer = 0;
        while(timer < 144)
        {   
            // update results from facebook
            carList = getListings();
            
            String car1 = carList.get(0).Title;
            String car2 = lastList.get(0).Title;

            //compare to last list. if changes, list them
            if(!car1.equals(car2))
            {
                // send notification of new ones
                List<Vehicle> newList = getNewListings(lastList, carList);
                String message = convertListings(newList);
                send_notification(message);
            }

            // update last list to current list
            lastList = carList;

            try{
                Thread.sleep(5*1000*60);
                timer++;
            }
            catch(Exception e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    public static String convertListings(List<Vehicle> list)
    {
        String message = "";
        for(Vehicle v : list)
        {
            message += v.Title;
            message += "    Price: ";
            message += v.Price;
            message += "    Milage: ";
            message += v.Milage;
            message += "       ";
        }
        return message;
    }

    public static List<Vehicle> getNewListings(List<Vehicle> lastList, List<Vehicle> carList)
    {
        List<Vehicle> returnThis = new ArrayList<Vehicle>();
        if(lastList.get(0) == carList.get(1))
        {
            returnThis.add(carList.get(1));
        }
        else if(lastList.get(0) == carList.get(2))
        {
            returnThis.add(carList.get(1));
            returnThis.add(carList.get(2));
        }
        else if(lastList.get(0) == carList.get(3))
        {
            returnThis.add(carList.get(1));
            returnThis.add(carList.get(2));
            returnThis.add(carList.get(3));
        }
        return returnThis;
    }

    public static void writeFile(List<Vehicle> list)
    {
        try
        {
            FileWriter myWriter = new FileWriter("/Users/luberdoodle/Desktop/cars.txt");
            
            for(Vehicle v : list)
            {
                ObjectMapper map = new ObjectMapper();
                String json = map.writeValueAsString(v);
                myWriter.write(json);
            }
            myWriter.close();
            

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void send_notification(String message)
    {
        try
        {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                .add("token", "aa9zfg1c7zx3nj6hy1rk1du2g8gimj")
                .add("user", "uwecykppyp4atsgz6ws6ivubdg59c4")
                .add("message", message)
                .build();
    
            Request request = new Request.Builder()
                    .url("https://api.pushover.net/1/messages.json")
                    .post(body)
                    .build();
    
            Call call = client.newCall(request);
            Response response = call.execute();

            System.out.println(response);
        }
        catch(Exception e)
        {

        }
    }

    public static List<Vehicle> getListings() 
    {
        List<Vehicle> cars = new ArrayList<Vehicle>();
        try {
            // Here we create a document object and use JSoup to fetch the website
            Document doc = Jsoup.connect(URL).get();

            // With the document fetched, we use JSoup's title() method to fetch the title
            System.out.printf("Title: %s\n", doc.title());

            Elements listings = doc.getElementsByClass(
                    "x9f619 x78zum5 xdt5ytf x1qughib x1rdy4ex xz9dl7a xsag5q8 xh8yej3 xp0eagm x1nrcals");

            int item = 1;
            for (Element listing : listings) 
            {
                String listingTitle = listing.getElementsByClass("xyqdw3p x4uap5 xjkvuk6 xkhd6sd").text();
                String listingPrice = listing.getElementsByClass("x78zum5 x1q0g3np x1iorvi4 x4uap5 xjkvuk6 xkhd6sd")
                        .text();
                String listingLocation = listing.getElementsByClass("x1iorvi4 x4uap5 xjkvuk6 xkhd6sd").text();
                String listingMilage = listing.getElementsByClass(
                        "x193iq5w xeuugli x13faqbe x1vvkbs xlh3980 xvmahel x1n0sxbx x1lliihq x1s928wv xhkezso x1gmr53x x1cpjm7i x1fgarty x1943h6x x4zkp8e x676frb x1nxh6w3 x1sibtaa xo1l8bm xi81zsa")
                        .text();

                Vehicle v = new Vehicle();
                v.Title = listingTitle;
                v.Price = listingPrice;
                v.Location = listingLocation;
                v.Milage = listingMilage;
                cars.add(v);

                System.out.println(v.Title);

                item++;
                if(item > 10)
                {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cars;
    }
}
