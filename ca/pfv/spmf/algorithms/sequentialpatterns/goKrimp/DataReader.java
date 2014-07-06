

package ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * This file reads data with different formats for the GoKrimp algorithm.
 * <br/><br/>
 * 
 * Copyright (c) 2014  Hoang Thanh Lam (TU Eindhoven and IBM Research)
 * Toon Calders (Université Libre de Bruxelles), Fabian Moerchen (Amazon.com inc)
 * and Dmitriy Fradkin (Siemens Corporate Research)
 * <br/><br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * <br/><br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <br/><br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <br/><br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoGoKrimp
 * @see Event
 * @see MyPattern
 * @see SignTest
*  @author  Hoang Thanh Lam (TU Eindhoven and IBM Research)
 */
public class DataReader {
    
    AlgoGoKrimp readData(String databasename, String labelfilename){
        AlgoGoKrimp gk=new AlgoGoKrimp();
        gk.labels=readLabel(labelfilename);
        gk.data=new ArrayList();
        try{
            DataInputStream in;
            FileInputStream fstream = new FileInputStream(databasename);
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            int size=0;
            while((strLine = br.readLine()) != null){
                String[] temp;
                String delimiter = " ";
                temp = strLine.split(delimiter);
                ArrayList<Event> s=new ArrayList();
                gk.data.add(s);
                int ts=0,prev=0;
                size++;
                for(int i=0;i<temp.length;i++){
                    Event e=new Event();
                    e.id=Integer.parseInt(temp[i]);
                    e.ts=ts;
                    e.gap=ts-prev;
                    prev=ts;
                    gk.data.get(gk.data.size()-1).add(e);
                    ts++;
                    /*if(ts%100==0)
                     System.out.println(e.id);
                    else
                     System.out.print(e.id+" ");*/
                }
               
            }
            System.err.println("data size:"+ size);
            in.close();
        }catch (IOException e){
                System.err.println("Error: " + e.getMessage());
        }
        
        return gk;
    }
    
    /**
     * read the data in the SPMF format
     * @param databasename
     * @return 
     */
    public AlgoGoKrimp readData_SPMF(String databasename, String labelfilename){
        AlgoGoKrimp gk=new AlgoGoKrimp();
        gk.labels=readLabel(labelfilename);
        gk.data=new ArrayList();
        try{
            DataInputStream in;
            FileInputStream fstream = new FileInputStream(databasename);
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while((strLine = br.readLine()) != null){
                String[] temp;
                String delimiter = " ";
                temp = strLine.split(delimiter);
                ArrayList<Event> s=new ArrayList();
                gk.data.add(s);
                int ts=0,prev=0;
                for(int i=0;i<temp.length;i++){
                    if(temp[i].contains("-"))
                        continue;
                    Event e=new Event();
                    e.id=Integer.parseInt(temp[i])-1;
                    e.ts=ts;
                    e.gap=ts-prev;
                    prev=ts;
                    gk.data.get(gk.data.size()-1).add(e);
                    ts++;                    
                }
               
            }           
            in.close();
        }catch (IOException e){
                System.err.println("Error: " + e.getMessage());
        }
        
        return gk;
    }
    
    HashMap<Integer,String> readLabel(String dataname){
        HashMap<Integer,String> labels= new HashMap();
        File file = new File(dataname);
        if(file.exists()){ //the label file with such name does not exist
            return labels;
        }
        try{
            DataInputStream in;
            FileInputStream fstream = new FileInputStream(dataname);
                in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int k=0;
                while((strLine = br.readLine()) != null){
                    labels.put(k, strLine);
                    k++;
                }
                in.close();
        }catch (IOException e){
                System.err.println("Warning: " + e.getMessage());
        }
        return labels;
     }         
}

