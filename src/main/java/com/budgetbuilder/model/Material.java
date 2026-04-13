package com.budgetbuilder.model;

public class Material {
    private String materialId;
    private String elmukCikkszam;
    private String eonCikkszam;
    private String megnevezese;
    private Double tervezettMennyiseg;
    private String mertkegyseg;
    private String megjegyzes;

    public Material(String materialId, String elmukCikkszam, String eonCikkszam, 
                    String megnevezese, Double tervezettMennyiseg, 
                    String mertkegyseg, String megjegyzes) {
        this.materialId = materialId;
        this.elmukCikkszam = elmukCikkszam;
        this.eonCikkszam = eonCikkszam;
        this.megnevezese = megnevezese;
        this.tervezettMennyiseg = tervezettMennyiseg;
        this.mertkegyseg = mertkegyseg;
        this.megjegyzes = megjegyzes;
    }

    // Getters and Setters
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }

    public String getElmukCikkszam() { return elmukCikkszam; }
    public void setElmukCikkszam(String elmukCikkszam) { this.elmukCikkszam = elmukCikkszam; }

    public String getEonCikkszam() { return eonCikkszam; }
    public void setEonCikkszam(String eonCikkszam) { this.eonCikkszam = eonCikkszam; }

    public String getMegnevezese() { return megnevezese; }
    public void setMegnevezese(String megnevezese) { this.megnevezese = megnevezese; }

    public Double getTervezettMennyiseg() { return tervezettMennyiseg; }
    public void setTervezettMennyiseg(Double tervezettMennyiseg) { this.tervezettMennyiseg = tervezettMennyiseg; }

    public String getMertkegyseg() { return mertkegyseg; }
    public void setMertkegyseg(String mertkegyseg) { this.mertkegyseg = mertkegyseg; }

    public String getMegjegyzes() { return megjegyzes; }
    public void setMegjegyzes(String megjegyzes) { this.megjegyzes = megjegyzes; }
}