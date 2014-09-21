package ir.ac.ut;

import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mosi
 */
public class Doc implements Serializable {
    String DOCID;
    String DATE;
    String CAT;
    String TEXT;
    Integer indexId;

    public Integer getIndexId() {
        return indexId;
    }

    public void setIndexId(Integer indexId) {
        this.indexId = indexId;
    }

    public Doc() {
        this.DOCID = "";
        this.DATE = "";
        this.CAT = "";
        this.TEXT = "";
    }

    public Doc(String DOCID, String DATE, String CAT, String TEXT) {
        this.DOCID = DOCID;
        this.DATE = DATE;
        this.CAT = CAT;
        this.TEXT = TEXT;
    }

    public void setDOCID(String DOCID) {
        this.DOCID = DOCID;
    }

    public void setDATE(String DATE) {
        this.DATE = DATE;
    }

    public void setCAT(String CAT) {
        this.CAT = CAT;
    }

    public void setTEXT(String TEXT) {
        this.TEXT = TEXT;
    }

    public String getDOCID() {
        return DOCID;
    }

    public String getDATE() {
        return DATE;
    }

    public String getCAT() {
        return CAT;
    }

    public String getTEXT() {
        return TEXT;
    }
    @Override
    public boolean equals(Object obj) {
        if(this.DOCID==((Doc)(obj)).DOCID)
            return true;
        return false;
    }

}
