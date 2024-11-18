package br.com.davidbuzatto.nonameplat.entities;

/**
 * Entity state.
 * 
 * @author Prof. Dr. David Buzatto
 */
public enum EntityState {
    
    IDLE,
    ON_GROUND,
    
    JUMPING,
    FALLING,
    
    WALKING,
    RUNNING,
    
    ALIVE,
    DYING,
    DEAD,
    
    ACTIVE,
    INACTIVE,
    
    ATTACKING,
    BEING_ATTACKED,
    
    CLIMBING,
    
    LOOKING_LEFT,
    LOOKING_RIGHT,
    LOOKING_UP,
    LOOKING_DOWN;
    
}
