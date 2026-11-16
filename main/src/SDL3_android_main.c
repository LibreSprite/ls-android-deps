#include <SDL3/SDL.h>
extern void extractAssets(void);
extern int main(int argc, char* argv[]);
int SDL_main(int argc, char* argv[]) {
    extractAssets();
    return main(argc, argv);
}
