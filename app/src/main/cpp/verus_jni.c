#include <jni.h>
#include <pthread.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>

/* miner entrypoint from libccminer.so */
extern int main(int argc, char **argv);

/* -----------------------
   CONFIG
----------------------- */

#define WALLET "REwQFzgjHryPUDk8ZmH2CByfznHKUGpnom"
#define POOL   "stratum+tcp://pool.verus.io:9999"

/* default values */

static int THREADS = 2;
static char WORKER[64] = "worker002";

static pthread_t miner_thread;
static pthread_t controller_thread;

static int running = 0;


/* -----------------------
   INTERNET CHECK
----------------------- */

int check_online(){

    int r = system("ping -c 1 -W 1 pool.verus.io > /dev/null 2>&1");

    return r == 0;
}


/* ------------------------
   Sleep schedule
------------------------ */

void get_sleep_times(int *run,int *sleep_time){

    switch(THREADS){

        case 2: case 3: case 4:
        case 5: case 6: case 7: case 8:

            *run = 900;
            *sleep_time = 360;
            break;

        case 1:

            *run = 2400;
            *sleep_time = 720;
            break;

        default:

            *run = 600;
            *sleep_time = 300;
    }
}


/* -----------------------
   MINER THREAD
----------------------- */

void* miner_run(void* arg){

    char threads_str[10];
    sprintf(threads_str,"%d",THREADS);

    char user[200];
    sprintf(user,"%s.%s",WALLET,WORKER);

    char *argv[] = {

        "ccminer",
        "-a","verus",
        "-o",POOL,
        "-u",user,
        "-p","x",
        "-t",threads_str,
        NULL
    };

    int argc = 9;

    /* run miner (prints its own logs) */
    main(argc, argv);

    return NULL;
}


/* -----------------------
   CONTROLLER THREAD
----------------------- */

void* controller_loop(void* arg){

    while(running){

        if(!check_online()){

            pthread_kill(miner_thread,SIGSTOP);

            sleep(30);

            continue;
        }

        int run_time;
        int sleep_time;

        get_sleep_times(&run_time,&sleep_time);

        sleep(run_time);

        pthread_kill(miner_thread,SIGSTOP);

        sleep(sleep_time);

        pthread_kill(miner_thread,SIGCONT);
    }

    return NULL;
}


/* -----------------------
   START MINER
----------------------- */

JNIEXPORT void JNICALL
Java_com_verusmine_VerusMiner_startMiner(JNIEnv *env,
                                         jobject thiz,
                                         jint threads,
                                         jstring worker){

    if(running) return;

    /* thread selection */

    if(threads > 0)
        THREADS = threads;
    else
        THREADS = 2;

    /* worker name */

    const char* w = (*env)->GetStringUTFChars(env, worker, 0);

    if(w != NULL && strlen(w) > 0)
        strncpy(WORKER,w,sizeof(WORKER)-1);
    else
        strcpy(WORKER,"worker002");

    (*env)->ReleaseStringUTFChars(env, worker, w);

    running = 1;

    pthread_create(&miner_thread,NULL,miner_run,NULL);

    pthread_create(&controller_thread,NULL,controller_loop,NULL);
}


/* -----------------------
   STOP MINER
----------------------- */

JNIEXPORT void JNICALL
Java_com_verusmine_VerusMiner_stopMiner(JNIEnv *env, jobject thiz){

    if(!running) return;

    running = 0;

    pthread_kill(miner_thread,SIGKILL);
}
