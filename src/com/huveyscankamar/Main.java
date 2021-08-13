package com.huveyscankamar;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// https://github.com/apache/echarts
// https://github.com/apache/superset
// https://github.com/apache/dubbo
// https://github.com/apache/spark
// https://github.com/apache/airflow

public class Main {

    static class Committer{
        private String login;
        private int count;

        public Committer(String login,int count){
            this.login = login;
            this.count = count;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getLogin() {
            return login;
        }

        public int getCount() {
            return count;
        }
    }

    // Swaps the Committer objects for sorting purposes
    public static void swap_committers(Committer a, Committer b){
        String temp_login = a.getLogin();
        a.setLogin(b.getLogin());
        b.setLogin(temp_login);

        int temp_count = a.getCount();
        a.setCount(b.getCount());
        b.setCount(temp_count);
    }

    // Sorts the Committer objects according to their number of commits
    public static List<Committer> sort_committers(List<Committer> committers){
        for(int i = 0;i<committers.size()-1;i++)
        {
            for(int j=i;j<committers.size();j++)
            {
                if(committers.get(i).getCount() < committers.get(j).getCount()){
                    swap_committers(committers.get(i),committers.get(j));
                }
            }
        }
        return committers;
    }

    // Returns the index of the commit owner from the list if it is already added to the set
    public static int getIndex(List<Committer> list, String value) {
        int result = 0;
        for (Committer entry:list) {
            if (entry.getLogin().equals(value)) return result;
            result++;
        }
        return -1;
    }

    // Creates a string as it was wanted in order to be written into a file
    public static String line_template_fitter(String repo, Committer committer, String location, String company ){
        return "repo:" + repo + ", user:" + committer.getLogin() + ", location:" +
                location + ", company:" + company + ", contributions:" + committer.getCount() + "\n";
    }

    // Writes the given strings into the related repo's file (creates, if it doesn't exist)
    private static void write_to_file(String repo, String text){
        File externalFile = new File(repo+".txt");
        FileOutputStream fos = null;
        if( text != null) {
            try {
                fos = new FileOutputStream(externalFile,true);
                fos.write(text.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    // Returns the sorted list of committers for the given repo
    public static List<Committer> return_committers(String url){
        JSONParser parser = new JSONParser();
        Set<String> committers_login_Set = new HashSet<String>();
        List<Committer> committers_count_List = new ArrayList<Committer>();
        boolean end_of_pages = false;
        try {
            int page_number = 1;
            int count = 0;
            while(!end_of_pages) {
                //URL oracle = new URL("http://127.0.0.1:5000/");
                URL oracle = new URL(url + "/commits?per_page=100&page=" + page_number++);
                URLConnection yc = oracle.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if(inputLine.equals("[]")){
                        end_of_pages = true;
                        break;
                    }
                    JSONArray a = (JSONArray) parser.parse(inputLine);
                    for (Object o : a) {
                        JSONObject commit = (JSONObject) o;
                        JSONObject author = (JSONObject) commit.get("author");
                        if (author != null) {
                            String login = (String) author.get("login");
                            if(!committers_login_Set.contains(login)){
                                committers_login_Set.add(login);
                                Committer committer = new Committer(login,1);
                                committers_count_List.add(committer);
                                count++;
                            }
                            else{
                                int i = getIndex(committers_count_List,login);
                                Committer committer = committers_count_List.get(i);
                                committer.setCount(committer.getCount() + 1);
                                committers_count_List.set(i,committer);
                            }
                        }
                    }
                    sort_committers(committers_count_List);
                }
                in.close();
                //end_of_pages = true; // Put to avoid reaching rate limit of Github during tests.
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return committers_count_List;
    }

    // Writes the top 10 most committers into a file
    public static void write_committer_to_file(String repo, List<Committer> committers){
        JSONParser parser = new JSONParser();
        int max = 10;
        if(committers.size()<10)
            max = committers.size();
        try {
            for(int i = 0;i<max;i++){
                //URL oracle = new URL("http://127.0.0.1:5000/users");
                URL oracle = new URL("https://api.github.com/users/" + committers.get(i).getLogin());
                URLConnection yc = oracle.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    JSONObject user = (JSONObject) parser.parse(inputLine);
                    String location = (String) user.get("location");
                    String company = (String) user.get("company");
                    String text = line_template_fitter(repo, committers.get(i), location, company);
                    write_to_file(repo, text);
                }
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String repo_prefix = "https://api.github.com/repos/apache/";
        List<String> repos = new ArrayList<String>();
        repos.add("echarts");
        repos.add("superset");
        repos.add("dubbo");
        repos.add("spark");
        repos.add("airflow");

        for(int i = 0;i<5;i++){
            List<Committer> committers = return_committers(repo_prefix + repos.get(i));
            write_committer_to_file(repos.get(i),committers);
        }
    }
}
