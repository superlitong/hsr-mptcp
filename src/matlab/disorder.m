function [ ret ] = disorder(data)
    ret = [];
    if isempty(data)
        disp('Empty data. Please check your data and try again.');
        return
    end
    
    ind = data(:,25) ~= 0;
    if sum(ind) == 0
        disp('Find no MP_SEQ. Please check and tray again.');
        return;
    end
    
    data = data(ind,:);
    data(:,28:32) = 0;
    expect = data(1,25);
    count = 0;
    
    for i = 1:size(data, 1)
        if data(i,25) == expect
            expect = data(i,25) + data(i,27);
        elseif data(i,25) > expect
            data(i,28) = 1;
            count = count + 1;
            for j = 1:i
                if data(j,28) == 1 && data(j,25) == expect
                    expect = data(j,25) + data(j,27);
                    data(j,28) = 2;
                    count = count - 1;
                    data(j,30) = i;
                    data(j,31) = data(i,1);
                    data(j,32) = data(i,1) - data(j,1);
                end
            end
        end
        data(i,29) = count;
    end
    ret = data;
end
