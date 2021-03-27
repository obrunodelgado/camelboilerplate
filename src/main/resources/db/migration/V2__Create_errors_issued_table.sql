CREATE TABLE public.errors_issued (
    system_time                 timestamptz
    ,route                      VARCHAR(100)
    ,message                    VARCHAR(4096)
    ,cause                      VARCHAR(4096)
    ,event                      VARCHAR(4096)
);