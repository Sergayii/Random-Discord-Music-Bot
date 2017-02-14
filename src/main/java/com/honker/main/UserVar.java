package com.honker.main;

import sx.blah.discord.handle.obj.IUser;

public class UserVar{
    
    public String name;
    public IUser user;
    
    public UserVar(IUser user){
        this.user = user;
        name = user.getName();
    }
}
