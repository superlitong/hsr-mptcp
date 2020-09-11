function ret = retrans(data)
ret = [];
if isempty(data)
    disp('Empty data! Please check your data!!!');
    return;
end

data(:,28) = 0;
% data(:,29) = 0;
% data(:,30) = 0;
maxseq = 0;
if data(1,5) < 0
    data(:,5) = uint32(data(:,5)+2^31);
end
for i = 1:size(data, 1)
    if data(i,7) == 2 && data(i,2) == 80
        maxseq = 0;
    end
    if data(i,5) < maxseq
        ind = data(:,5)==data(i,5);
        if sum(ind~=0) <= 1
            continue;
        end
        ret = [ ret; data(ind,:) ];
        continue;
    end
    if data(i,2) == 80 && data(i,5) >= maxseq
        maxseq = data(i,5);
    end
end
ret = unique(ret, 'rows');
if ~isempty(ret)
    ret = sortrows(ret, 5);
end

for i = 2:size(ret, 1)
   if ret(i,5) == ret(i-1,5)
       ret(i,28) = ret(i,1) - ret(i-1,1);
   end
end
end
