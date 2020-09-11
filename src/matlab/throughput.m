function [ ret ] = throughput(data, dt)
    ret = [];
    
    if isempty(data)
        disp('Empty data! Please check your data and try again.');
        return;
    end
    
    for tt = data(1,1) : dt : data(end,1)
        ind = data(:,1) >= tt & data(:,1) < (tt + dt);
        ret = [ ret; [ tt sum(ind) sum(data(ind,4)) sum(data(ind,4)) / dt ] ];
    end
end
