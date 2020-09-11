%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% An example to show how these codes work %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clear;
clc;

prefix = 'E:/sigcomm/data/';
m_log = 'result/2017.03.02/MT/M.2017.03.02.log';
t_log = 'result/2017.03.02/MT/T.2017.03.02.log';
mt_log = 'result/2017.03.02/MT/MT.2017.03.02.log';
 
m_thp = deal_thp_1(prefix, m_log);
t_thp = deal_thp_1(prefix, t_log);
[ mu_t_thp, mu_m_thp, mu_tot_thp ] = deal_thp_2(prefix, mt_log);

m_rtt = deal_rtt_1(prefix, m_log);
u_rtt = deal_rtt_1(prefix, u_log);
[ mu_m_rtt, mu_u_rtt ] = deal_rtt_2(prefix, mu_log);

m_bif = deal_bif_1(prefix, m_log);
u_bif = deal_bif_1(prefix, u_log);
[ mu_m_bif, mu_u_bif ] = deal_bif_2(prefix, mu_log);

m_dis = deal_dis_1(prefix, m_log);
u_dis = deal_dis_1(prefix, u_log);
mu_dis = deal_dis_2(prefix, mu_log);

m_lr = deal_lr_1(prefix, m_log);
u_lr = deal_lr_1(prefix, u_log);
mu_lr = deal_lr_2(prefix, mu_log);

m_lpr = lr2lpr(m_lr, m_log, 1, []);
u_lpr = lr2lpr(u_lr, u_log, 1, x);
mu_lpr = lr2lpr(mu_lr, mu_log, 2, y);

% @usage: open log file
% @param filename: log file name
% @return: return the matrix of the data recording in `filename`
function [ ret ] = open_log(filename)
    if ~exist(filename, 'file')
        fprintf('Cannot open %s\n', filename);
        ret = [];
    else
        fid = fopen(filename, 'r');
        % global-id(%f) temp-id(%f) carr-type(%f) expr-type(%f) stt-ts(%f) 
        % end-ts(%f) cli-pcap(%s) cli-thp(%s) ser-pcap(%s) carrier(%s) 
        % filesize(%f) cli-txt(%s) ser-txt(%s) train-id(%f)
        ret = textscan(fid, '%f %f %f %f %f %f %s %s %s %s %f %s %s %f');
        fclose(fid);
    end
end

% @usage: calculate RTT for flows with only on subflow
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the RTT for each packet
function [ ret_rtt ] = deal_rtt_1(prefix, filename)    
    record = open_log(filename);
    if isempty(record) return; end

    ret_rtt = [];

    for i = 1:size(record{1}, 1)
        id = record{1}(i);   
        type = record{4}(i);
        train = record{14}(i);
        path = record{12}{i};

        fprintf([ 'Processing #', num2str(id), ' experiment\n' ]);
        stream = txt2stream([ prefix, path ], 1);

        if isempty(stream)
            fprintf('Please check the path of server text file existence.\n');
            return;
        end

        fprintf('Processing 1st RTT...\n');
%         tmp_rtt = clientRtt(sortrows([ stream{1}; stream{2} ]));
        tmp_rtt = rtt(sortrows([ stream{1}; stream{2} ]));
        % [ ts s-port sack[0] rtt 0 exp_id exp_type ]
        if ~isempty(tmp_rtt)
            ret_rtt = [ ret_rtt; ...
                [ tmp_rtt(:,1) tmp_rtt(:,2) tmp_rtt(:,11) tmp_rtt(:,29) ...
                id * ones(size(tmp_rtt(:,1))) ...
                type * ones(size(tmp_rtt(:,1))) ...
                train * ones(size(tmp_rtt(:,1)))]];
        end
    end
end

% @usage: calculate RTT for flows with two subflows
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the RTT for each packet
%          ret_rtt_1: the RTT of the 1st subflow
%          ret_rtt_2: the RTT of the 2nd subflow
function [ ret_rtt_1, ret_rtt_2 ] = deal_rtt_2(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_rtt_1 = [];
    ret_rtt_2 = [];

    for i = 1:2:size(record{1}, 1)
        id = record{1}(i);   
        type = record{4}(i);
        train = record{14}(i);
        path_1 = record{12}{i};
        path_2 = record{12}{i+1};
        
        fprintf([ 'Processing #', num2str(id), ' experiment\n' ]);
        stream_1 = txt2stream([ prefix, path_1 ], 1);
        stream_2 = txt2stream([ prefix, path_2 ], 1);

        if isempty(stream_1) || isempty(stream_2)
            fprintf('Please check the path of server text file existence.\n');
            return;
        end

        fprintf('Processing 1st RTT...\n');
        tmp_rtt = rtt(sortrows([ stream_1{1}; stream_1{2} ]));
        % [ ts s-port sack[0] rtt 0 exp_id exp_type ]
        if ~isempty(tmp_rtt)
            ret_rtt_1 = [ ret_rtt_1; ...
                [ tmp_rtt(:,1) tmp_rtt(:,2) tmp_rtt(:,11) tmp_rtt(:,29) ...
                id * ones(size(tmp_rtt(:,1))) ...
                type * ones(size(tmp_rtt(:,1))) ...
                train * ones(size(tmp_rtt(:,1)))]];
        end
        fprintf('Processing 2nd RTT...\n');
        tmp_rtt = rtt(sortrows([ stream_2{1}; stream_2{2} ]));
        % [ ts s-port sack[0] rtt 0 exp_id exp_type ]
        if ~isempty(tmp_rtt)
            ret_rtt_2 = [ ret_rtt_2; ...
                [ tmp_rtt(:,1) tmp_rtt(:,2) tmp_rtt(:,11) tmp_rtt(:,29) ...
                id * ones(size(tmp_rtt(:,1))) ...
                type * ones(size(tmp_rtt(:,1))) ...
                train * ones(size(tmp_rtt(:,1)))]];
        end
    end
end

% @usage: calculate throughput for flows with only one subflow
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the throughput for each packet
function [ ret_thp ] = deal_thp_1(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_thp = [];

    for i = 1:size(record{1}, 1)
        global_id = record{1}(i); 
        expr_id = record{2}(i);
        type = record{4}(i);
        train = record{14}(i);
        path = record{12}{i};

        fprintf([ 'Processing #', num2str(global_id), ' experiment\n' ]);
        stream = txt2stream([ prefix, path ], 1);

        if isempty(stream)
            fprintf('Please check the path of client text file existence.\n');
            return;
        end

        fprintf('Processing 1st throughput...\n');
        thp = throughput(sortrows([ stream{1}; stream{2} ]), 1);
        % [ ts aver_thp exp_id exp_type train_id ]
        if ~isempty(thp)
            ret_thp = [ ret_thp; ...
                [ thp(:,1) thp(:,4) ...
                global_id * ones(size(thp(:,1))) ...
                expr_id * ones(size(thp(:,1))) ...
                type * ones(size(thp(:,1))) ...
                train * ones(size(thp(:,1)))]];
        end
    end
end

% @usage: calculate throughput for flows with two subflows
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the throughput for each time plot
%          ret_thp_1: the throughput of the 1st subflow
%          ret_thp_2: the throughput of the 2nd subflow
%          total_thp: the throughput summary of two subflows
function [ ret_thp_1, ret_thp_2, total_thp ] = deal_thp_2(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_thp_1 = [];
    ret_thp_2 = [];
    total_thp = [];

    for i = 1:2:size(record{1}, 1)
        global_id = record{1}(i);   
        expr_id = record{2}(i);
        type = record{4}(i);
        train = record{14}(i);
        path_1 = record{12}{i};
        path_2 = record{12}{i+1};

        fprintf([ 'Processing #', num2str(global_id), ' experiment\n' ]);
        stream_1 = txt2stream([ prefix, path_1 ], 1);
        stream_2 = txt2stream([ prefix, path_2 ], 1);

        if isempty(stream_1) || isempty(stream_2)
            fprintf('Please check the path of client text file existence.\n');
            return;
        end

        fprintf('Processing 1st throughput...\n');
        thp = throughput(sortrows([ stream_1{1}; stream_1{2} ]), 1);
        % [ ts aver_thp exp_id exp_type train_id ]
        if ~isempty(thp)
            ret_thp_1 = [ ret_thp_1; ...
                [ thp(:,1) thp(:,4) ...
                global_id * ones(size(thp(:,1))) ...
                expr_id * ones(size(thp(:,1))) ...
                type * ones(size(thp(:,1))) ...
                train * ones(size(thp(:,1)))]];
        end
        fprintf('Processing 2nd throughput...\n');
        thp = throughput(sortrows([ stream_2{1}; stream_2{2} ]), 1);
        % [ ts aver_thp exp_id exp_type train_id ]
        if ~isempty(thp)
            ret_thp_2 = [ ret_thp_2; ...
                [ thp(:,1) thp(:,4) ...
                global_id * ones(size(thp(:,1))) ...
                expr_id * ones(size(thp(:,1))) ...
                type * ones(size(thp(:,1))) ...
                train * ones(size(thp(:,1)))]];
        end
        fprintf('Processing Total throughput...\n');
        thp = throughput(sortrows( ...
            [ stream_1{1}; stream_1{2}; stream_2{1}; stream_2{2} ]), 1);
        % [ ts aver_thp exp_id exp_type train_id ]
        if ~isempty(thp)
            total_thp = [ total_thp; ...
                [ thp(:,1) thp(:,4) ...
                global_id * ones(size(thp(:,1))) ...
                expr_id * ones(size(thp(:,1))) ...
                type * ones(size(thp(:,1))) ...
                train * ones(size(thp(:,1)))]];
        end
    end
end

% @usage: calculate in-flight for flows with only one subflows
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the in-flight for each flow
function [ ret_bif ] = deal_bif_1(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_bif = [];

    for i = 1:size(record{1}, 1)
        id = record{1}(i);   
        type = record{4}(i);
        train = record{14}(i);
        path = record{13}{i};

        fprintf([ 'Processing #', num2str(id), ' experiment\n' ]);
        stream = txt2stream([ prefix, path ], 1);

        if isempty(stream)
            fprintf('Please check the path of client text file existence.\n');
            return;
        end

        fprintf('Processing 1st Bytes-In-Flight...\n');
        bif = cwnd(sortrows([ stream{1}; stream{2} ]));
        % [ ts s-port d-port bit exp_id exp_type ]
        if ~isempty(bif)
            ret_bif = [ ret_bif; ...
                [ bif(:,1) bif(:,2) bif(:,3) bif(:,28) ...
                id * ones(size(bif(:,1))) ...
                type * ones(size(bif(:,1))) ...
                train * ones(size(bif(:,1)))]];
        end
    end
end

% @usage: calculate in-flight for flows with two subflows
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the in-flight for each packet
%          ret_bif_1: the in-flight of the 1st subflow
%          ret_bif_2: the in-flight of the 2nd subflow
function [ ret_bif_1, ret_bif_2 ] = deal_bif_2(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_bif_1 = [];
    ret_bif_2 = [];

    for i = 1:2:size(record{1}, 1)
        id = record{1}(i);   
        type = record{4}(i);
        train = record{14}(i);
        path = record{13}{i};

        fprintf([ 'Processing #', num2str(id), ' experiment\n' ]);
        stream = txt2stream([ prefix, path ], 2);

        if isempty(stream)
            fprintf('Please check the path of client text file existence.\n');
            return;
        end

        fprintf('Processing 1st Bytes-In-Flight...\n');
        bif = cwnd(sortrows([ stream{1}; stream{2} ]));
        % [ ts s-port d-port bit exp_id exp_type ]
        if ~isempty(bif)
            ret_bif_1 = [ ret_bif_1; ...
                [ bif(:,1) bif(:,2) bif(:,3) bif(:,28) ...
                id * ones(size(bif(:,1))) ...
                type * ones(size(bif(:,1))) ...
                train * ones(size(bif(:,1)))]];
        end
        fprintf('Processing 2nd Bytes-In-Flight...\n');
        bif = cwnd(sortrows([ stream{3}; stream{4} ]));
        % [ ts s-port d-port bit exp_id exp_type ]
        if ~isempty(bif)
            ret_bif_2 = [ ret_bif_2; ...
                [ bif(:,1) bif(:,2) bif(:,3) bif(:,28) ...
                id * ones(size(bif(:,1))) ...
                type * ones(size(bif(:,1))) ...
                train * ones(size(bif(:,1)))]];
        end
    end
end

% @usage: calculate loss packets for flows with only one subflows
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the loss packets for each packet
function [ ret_lr ] = deal_lr_1(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_lr = [];

    for i = 1:size(record{1}, 1)
        id = record{1}(i);   
        type = record{4}(i);
        c_path = record{12}{i};
        s_path = record{13}{i};
        train = record{14}(i);
        
        fprintf([ 'Processing #', num2str(id), ' experiment\n' ]);
        s_stream = txt2stream([ prefix, s_path ], 1);
        c_stream = txt2stream([ prefix, c_path ], 1);

        if isempty(s_stream) || isempty(c_stream)
            fprintf('Please check the path of client and server text file existence.\n');
            return;
        end

        fprintf('Processing 1st Lose Rate...\n');
        lr = lossRate(sortrows([ c_stream{1}; c_stream{2} ]), ...
                      sortrows([ s_stream{1}; s_stream{2} ]));
        % [ lr exp_id exp_type train_id]
        if ~isempty(lr)
            ret_lr = [ ret_lr; ...
                [ lr ...
                id * ones(size(lr(:,1))) ...
                type * ones(size(lr(:,1))) ...
                train * ones(size(lr(:,1)))]];
        end
    end
end

% @usage: calculate loss packets for flows with two subflows
% @param prefix: the path where we store data filesize
% @param filename: log file name
% @return: return the matrix of the summary of loss packets
function [ ret_lr ] = deal_lr_2(prefix, filename)
    record = open_log(filename);
    if isempty(record) return; end

    ret_lr = [];

    for i = 1:2:size(record{1}, 1)
        id = record{1}(i);   
        type = record{4}(i);
        train = record{14}(i);
        c_path_1 = record{12}{i};
        c_path_2 = record{12}{i+1};
        s_path = record{13}{i};

        fprintf([ 'Processing #', num2str(id), ' experiment\n' ]);
        s_stream = txt2stream([ prefix s_path ], 2);
        c_stream_1 = txt2stream([ prefix, c_path_1 ], 1);
        c_stream_2 = txt2stream([ prefix, c_path_2 ], 1);

        if isempty(c_stream_1) || isempty(c_stream_2) || isempty(s_stream)
            fprintf('Please check the path of client text file existence.\n');
            return;
        end

        fprintf('Processing 1st and 2nd Disorder-Number...\n');
        lr = lossRate(sortrows([ c_stream_1{1}; c_stream_1{2}; ...
                                 c_stream_2{1}; c_stream_2{2} ]), ...
                      sortrows([ s_stream{1}; s_stream{2}; ...
                                 s_stream{3}; s_stream{4}]));
        % [ lr exp_id exp_type train_id]
        if ~isempty(lr)
            ret_lr = [ ret_lr; ...
                [ lr ...
                id * ones(size(lr(:,1))) ...
                type * ones(size(lr(:,1))) ...
                train * ones(size(lr(:,1)))]];
        end
    end
end
