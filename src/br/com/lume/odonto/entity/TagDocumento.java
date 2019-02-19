package br.com.lume.odonto.entity;

import java.io.Serializable;

public class TagDocumento implements Serializable, Comparable<TagDocumento> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String tag;

    private String respTag;

    public String getTag() {

        return this.tag;
    }

    public void setTag(String tag) {

        this.tag = tag;
    }

    public String getRespTag() {

        return this.respTag;
    }

    public void setRespTag(String respTag) {

        this.respTag = respTag;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.tag == null) ? 0 : this.tag.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TagDocumento other = (TagDocumento) obj;
        if (this.tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!this.tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TagDocumento arg0) {

        return this.getTag().compareTo(arg0.getTag());
    }

}
