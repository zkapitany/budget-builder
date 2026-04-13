package com.budgetbuilder.model;

public class WorkItem {
    private String workId;
    private String eonCikkszam;
    private String bszjAzonosito;
    private String elszamolaTetelmegnevezese;
    private Double tervezettMennyiseg;
    private String mertkegyseg;
    private String megjegyzes;

    public WorkItem(String workId, String eonCikkszam, String bszjAzonosito,
                    String elszamolaTetelmegnevezese, Double tervezettMennyiseg,
                    String mertkegyseg, String megjegyzes) {
        this.workId = workId;
        this.eonCikkszam = eonCikkszam;
        this.bszjAzonosito = bszjAzonosito;
        this.elszamolaTetelmegnevezese = elszamolaTetelmegnevezese;
        this.tervezettMennyiseg = tervezettMennyiseg;
        this.mertkegyseg = mertkegyseg;
        this.megjegyzes = megjegyzes;
    }

    // Getters and Setters
    public String getWorkId() { return workId; }
    public void setWorkId(String workId) { this.workId = workId; }

    public String getEonCikkszam() { return eonCikkszam; }
    public void setEonCikkszam(String eonCikkszam) { this.eonCikkszam = eonCikkszam; }

    public String getBszjAzonosito() { return bszjAzonosito; }
    public void setBszjAzonosito(String bszjAzonosito) { this.bszjAzonosito = bszjAzonosito; }

    public String getElszamolaTetelmegnevezese() { return elszamolaTetelmegnevezese; }
    public void setElszamolaTetelmegnevezese(String elszamolaTetelmegnevezese) { 
        this.elszamolaTetelmegnevezese = elszamolaTetelmegnevezese; 
    }

    public Double getTervezettMennyiseg() { return tervezettMennyiseg; }
    public void setTervezettMennyiseg(Double tervezettMennyiseg) { this.tervezettMennyiseg = tervezettMennyiseg; }

    public String getMertkegyseg() { return mertkegyseg; }
    public void setMertkegyseg(String mertkegyseg) { this.mertkegyseg = mertkegyseg; }

    public String getMegjegyzes() { return megjegyzes; }
    public void setMegjegyzes(String megjegyzes) { this.megjegyzes = megjegyzes; }
}