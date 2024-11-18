package br.com.davidbuzatto.nonameplat.entities;

/**
 * Game Entity.
 * 
 * @author Prof. Dr. David Buzatto
 */
public abstract class Entity {
    
    private static int idCounter;
    protected int id;
    
    public Entity() {
        id = idCounter++;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Entity other = (Entity) obj;
        return this.id == other.id;
    }
    
}
