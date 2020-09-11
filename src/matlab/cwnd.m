function ret = cwnd(data)
    ret = [];
    if isempty(data)
        disp('Empty data! Please check your data!!!');
        return;
    end
    
%     ind = data(:,21) == 30;
%     data = data(ind,:);
    data(:,28) = 0;
    data(:,29) = 0;
    data(:,30) = 0;
    set = 0;
    
    for i = 1:size(data, 1)
        if data(i,7) == 2 && data(i,3) == 80
            maxseq = 0;
            ackseq = 0;
            set = 0;
        end
        if data(i,2) == 80 && set == 0
            maxseq = data(i,5);
            ackseq = data(i,5);
            set = 1;
        end
        if data(i,2) == 80
            maxseq = max(maxseq, data(i,5) + data(i,4));
            data(i,28) = maxseq - ackseq;
            data(i,29) = maxseq;
            data(i,30) = ackseq;
        elseif data(i,3) == 80
            if data(i,11) == 0
                ackseq = data(i,6);
            else
                ackseq = data(i,6) + data(i,12) - data(i,11) ...
                    +data(i,14) - data(i,13) + data(i,16) - data(i,15);
            end
        end
        
        data(i,28) = maxseq - ackseq;
        data(i,29) = maxseq;
        data(i,30) = ackseq;
    end
    
%     data(data(:,28) < 0, 28) = 0;
    ret = data;
end
