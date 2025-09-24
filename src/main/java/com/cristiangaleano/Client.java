package com.cristiangaleano;

import java.util.Random;
import java.util.Scanner;

public class Client 
{
    public static void main( String[] args )
    {
    
        Scanner scanner = new Scanner(System.in);
       int idealTemperature = 22;
       int currentTemperature;
       int counter = 0;
       int maxAttempts = scanner.nextInt();

        while (counter < maxAttempts) {
            currentTemperature = RandomTemperature.getRandomTemperature();
            System.out.println(currentTemperature);
            counter++; 
        }

        scanner.close();
    }

    private static class RandomTemperature{
        public static int getRandomTemperature(){
            Random random = new Random();
            int min = -10;
            int max = 55;
            return random.nextInt(max - min + 1) + min;
        }
    }
}
