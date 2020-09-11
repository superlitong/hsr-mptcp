function [ stream ] = txt2stream(filepath, cnt)  
    stream = cell(cnt * 2, 1);
    if ~exist(filepath)
        fprintf('Cannot open file %s: Not exist\n', filepath);
        return
    else 
        disp(filepath);
    end
    data = load(filepath); 
    if isempty(data)
        return
    end

    ports = unique(data(:,2:3), 'rows');
    ret = zeros(size(ports, 1), 3);
    
    for i = 1:size(ports, 1)
        ind = data(:,2) == ports(i,1) & data(:,3) == ports(i,2);
        ret(i,:) = [ ports(i,:) sum(ind) ];
    end
    
    tmp = [];
    for i = 1:size(ret, 1) - 1
        if (ret(i,3) < 0)
            continue;
        end
        ind = ret(:,1) == ret(i,2) & ret(:,2) == ret(i,1);
        tmp = [ tmp; [ ret(i,1:2) ret(i,3) + sum(ret(ind,3)) ] ];
        ret(ind,3) = -1;
    end
    
    if isempty(tmp)
        return;
    end
    
    tmp = flipud(sortrows(tmp, 3));
    tmp = max(tmp(1:cnt,1:2), [], 2);
    for i = 1:cnt
        ind = data(:,2) == tmp(i) & data(:,3) == 80;
        stream{2 * i - 1} = data(ind,:);
        ind = data(:,2) == 80 & data(:,3) == tmp(i);
        stream{2 * i} = data(ind,:);
    end
end
