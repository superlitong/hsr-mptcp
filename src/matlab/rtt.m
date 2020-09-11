% function [ ret, data, sacklist ] = rtt(data_path)
function [ ret ] = rtt(data)
%     data = load(data_path);
    ret = [];
    sacklist = [];
    if isempty(data)
        disp('Find no data. Please check the data existence.');
        return;
    end
    
    data(:,28) = 0;
    data(:,29) = 0;
    v = 250;
    
    ind = data(:,7) == 2; % Search for the first SYN/ACK packet
    synack = data(ind,:);
    if isempty(synack)
        ind = data(:,7) == 16 & data(:,3) == 80;
        synack = data(ind,:);
        if isempty(synack)
            disp('Find no SYN/ACK packet. Return!!!');
            return;
        end
    end
    gap = synack(1,1) - synack(1,9) / v;
    
    for i = 2:size(data, 1)
        if data(i,2) ~= 80
            continue;
        end
        
        if data(i,9) == 0
            continue;
        end
        
        if data(i,11) == 0
            data(i,28) = 0;
            data(i,29) = data(i,1) - (gap + data(i,10) / v);
        elseif data(i,11) > data(i,6)
            ind = data(:,3) == 80 & data(:,4) + data(:,5) == data(i,12);
            tmp = data(ind,:);
            if isempty(tmp)
                continue;
            end
            data(i,28) = tmp(end,9);
            data(i,29) = data(i,1) - (gap + data(i,28) / v);
            sacklist = [ sacklist; data(i,:) ];
        end
    end
    ret = data(data(:,2) == 80,:);
end
