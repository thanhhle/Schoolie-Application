package com.school.schoolie.model;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SCalendar implements Comparable<String>{
    private static Calendar cal;
    private static GregorianCalendar gCal;
    private int year;
    private int month;
    private int day;
    private int totalWeeksInMonth;
    private int totalDaysInMonth;
    private int startInWeek;
    private ArrayList<String> dates;


    public SCalendar(){
        cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        setDate(year,month);

    }
    private void setDate(int y, int m){
        gCal = new GregorianCalendar(y,m,1);
        totalDaysInMonth = gCal.getActualMaximum(Calendar.DATE);
        startInWeek = gCal.get(Calendar.DAY_OF_WEEK);

        gCal= new GregorianCalendar(y,m,totalDaysInMonth);
        totalWeeksInMonth = gCal.getActualMaximum(Calendar.WEEK_OF_MONTH);

    }
    /**
     * Set calendar object to the next month
     */
    public void monthForward(){
        if(month ==11){
            month = 0;
            year++;
        }else{
            month++;
        }
        setDate(year,month);
    }
    /**
     * Set calendar object to the last month
     */
    public void monthBack(){
        if(month == 0){
            month = 11;
            year--;
        }else{
            month--;
        }
        setDate(year,month);
    }
    public int getDayNum(){
        return day;
    }
    public int getMonthNum(){
        return month + 1;
    }
    public int getYearNum(){
        return year;
    }
    public String[] getMonthsLONG(){
        String []months = {"January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October",
                "November", "December"};
        return months;
    }
    public String[] getMonthShort(){
        String []months = {"JAN", "FEB", "MAR", "APR", "MAY",
                "JUN", "JUL", "AUG", "SEP", "OCT",
                "NOV", "DEC"};
        return months;
    }
    public String []getDays(){
        String []days = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        return days;
    }
    public String []getDaysShort(){
        String []days = {"SU","MO","TU","WE","TH","FR","SA"};
        return days;
    }

    public void print(){
        getDates();
        String []days = getDaysShort();
        String []months = getMonthsLONG();

        System.out.println("\t  " + months[getMonthInteger()] + "\n");

        for(int i =0; i < 7; i++){
            System.out.print(days[i] + "  ");
        }
        System.out.println();
        for(int i = 0; i < totalWeeksInMonth; i++){
            System.out.println(dates.get(i));
        }
        System.out.println();
    }
    public int getMonthInteger(){
        return month;
    }

    public String getMonth(){
        String months[] = getMonthsLONG();
        return months[month];
    }
    public int getCurrentDay(){
        return day;
    }
    public String getYear(){
        return String.valueOf(year);
    }
    public int getTotalWeeks(){
        return totalWeeksInMonth;
    }

    /**
     * Days are separated with ",". Each element in ArrayList contains one week of dates.(I.E element[0] contain days 1-7 of June)
     * Hint: Use .split(",") to separate the days. Use getTotalWeeks() as a limit in for loop to print out total days per week.
     * @return ArrayList of Week with day
     */
    public ArrayList<String> getDates(){
        dates = new ArrayList<String>();
        int count = 1;
        String week;

        for(int i = 0; i < totalWeeksInMonth; i++){
            week = "";
            for(int j=0; j < 7; j++){
                int protentialDate = count - startInWeek +1;
                if(count < startInWeek || (protentialDate) > totalDaysInMonth){
                    week += "___,";
                }else{
                    if(protentialDate < 10){
                        week += " "+ String.valueOf(protentialDate) + ",";
                    }else{
                        week += String.valueOf(protentialDate) + ",";
                    }
                }

                count++;
            }
            dates.add(week);
        }
        return dates;
    }

    public String getDateToday(){
        return String.valueOf(year)+ String.valueOf(month + 1)+ String.valueOf(day);
    }
    // Return long version of today i.e. November  21, 2020
    public String getDateTodayLONG(){
        return getMonth() + " " + getDayNum() + ", " + getYear();
    }
    public String getDateFromTodayLONG(int numOfDaysToAdd){
        cal.add(Calendar.DATE, numOfDaysToAdd);
        // set to midnight
        Date y = cal.getTime();
        String shortMonths[] = getMonthShort();
        String longMonths[] = getMonthsLONG();

        String yesterdayArr[] = y.toString().split(" ");
        String shortMonth = yesterdayArr[1].toUpperCase();
        String day = yesterdayArr[2];
        String year = yesterdayArr[5];

        if(day.charAt(0) == '0'){
            day = day.substring(1,2);
        }
        //converting abbreviated month to regular spelling
        for(int i = 0; i < shortMonths.length; i++){
            if(shortMonths[i].equals(shortMonth)) {
                shortMonth = longMonths[i];
            }
        }

        return shortMonth + " "+ day + ", " + year;
    }
    //greater than returns 1, less than returns -1, same date returns 0
    @Override
    public int compareTo(String o){
        String y = o.substring(0,4);
        String m = o.substring(4,6);
        String d = o.substring(6);

        if(Integer.valueOf(year) > Integer.valueOf(y)){
            return 1;
        }else if(Integer.valueOf(year) == Integer.valueOf(y)){
            if(Integer.valueOf(month) > Integer.valueOf(m)) {
                return 1;
            }else if(Integer.valueOf(month) == Integer.valueOf(m)){
                if(Integer.valueOf(day) > Integer.valueOf(d)){
                    return 1;
                }else if(Integer.valueOf(day) == Integer.valueOf(d)){
                    return 0;
                }else{
                    return -1;
                }
            }else{
                return -1;
            }
        }
        else{
            return -1;
        }
    }
}
