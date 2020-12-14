package com.school.schoolie.model;

public class GradeItem
{
    private String pointAchieved;
    private String totalPoint;
    private String weight;
    private String weightAchieved;
    private String grade;


    public GradeItem()
    {

    }

    public GradeItem(String pointAchieved, String totalPoint, String weight)
    {
        this.pointAchieved = pointAchieved;
        this.totalPoint = totalPoint;
        this.weight = weight;

        int p = new Integer(this.pointAchieved);
        int t = new Integer(this.totalPoint);
        int w = new Integer(this.weight);

        this.weightAchieved = String.valueOf(w*p/t);
        this.grade = String.valueOf(p*100/t);

    }

    public String getPointAchieved()
    {
        return this.pointAchieved;
    }

    public String getTotalPoint()
    {
        return this.totalPoint;
    }

    public String getWeight()
    {
        return this.weight;
    }

    public String getWeightAchieved()
    {
        return this.weightAchieved;
    }

    public String getGrade()
    {
        return this.grade;
    }
}
