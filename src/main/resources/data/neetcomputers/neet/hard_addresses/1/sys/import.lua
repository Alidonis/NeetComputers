return function(path)
    if files.exists(path) then
        local datFile = files.open(path,"r")
        local dat = datFile.read("a")
        local prog = load(dat,path)
        if prog then
            local worked, progFunc = pcall(prog)
            if not worked then
                error("Failure while loading program "..path.."! Err="..progFunc);
            else
                return progFunc
            end
        else
            error("Failed to load program "..path.."!")
        end
    else
        error("File "..path.." does not exist!")
    end
end